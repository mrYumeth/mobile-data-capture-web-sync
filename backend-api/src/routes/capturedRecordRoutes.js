const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const prisma = require('../config/prisma');
const {
  getSupabaseClient,
  isSupabaseStorageConfigured,
  storageBucket,
} = require('../config/supabase');

const router = express.Router();

const localUploadFolder = path.join(
  __dirname,
  '../../uploads/captured-images'
);

const signedUrlExpiresInSeconds = Number(
  process.env.SUPABASE_SIGNED_URL_EXPIRES_IN || 60 * 60
);

if (!fs.existsSync(localUploadFolder)) {
  fs.mkdirSync(localUploadFolder, { recursive: true });
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024,
  },
});

function isHttpUrl(value) {
  return /^https?:\/\//i.test(value || '');
}

function isSupabaseStoragePath(storagePath) {
  return Boolean(
    isSupabaseStorageConfigured() &&
      storagePath &&
      !isHttpUrl(storagePath) &&
      (
        storagePath.startsWith('captured-records/') ||
        storagePath.startsWith('tenants/')
      )
  );
}

function buildImageUrl(req, imageUrl) {
  if (!imageUrl) {
    return null;
  }

  if (isHttpUrl(imageUrl)) {
    return imageUrl;
  }

  return `${req.protocol}://${req.get('host')}${imageUrl}`;
}

async function createSignedImageUrl(storagePath) {
  if (!isSupabaseStoragePath(storagePath)) {
    return null;
  }

  const supabase = getSupabaseClient();

  const { data, error } = await supabase.storage
    .from(storageBucket)
    .createSignedUrl(storagePath, signedUrlExpiresInSeconds);

  if (error) {
    throw new Error(`Failed to create signed image URL: ${error.message}`);
  }

  return data.signedUrl;
}

async function resolveImageUrl(req, imageUrl, storagePath) {
  if (isSupabaseStoragePath(storagePath)) {
    try {
      return await createSignedImageUrl(storagePath);
    } catch (error) {
      console.error(error.message);
      return null;
    }
  }

  return buildImageUrl(req, imageUrl);
}

function getUploadedImageFiles(req) {
  const multipleImages = req.files?.images || [];
  const legacyImage = req.files?.image || [];

  return [...multipleImages, ...legacyImage];
}

function parsePositiveInteger(value) {
  const parsedValue = Number(value);

  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    return null;
  }

  return parsedValue;
}

function toOptionalDecimalValue(value) {
  if (value === undefined || value === null || value === '') {
    return null;
  }

  const numericValue = Number(value);

  if (!Number.isFinite(numericValue)) {
    return undefined;
  }

  return value.toString();
}

async function uploadImageFile(file, tenantId) {
  const extension = path.extname(file.originalname) || '.jpg';
  const fileName = `${Date.now()}-${crypto.randomUUID()}${extension}`;

  if (isSupabaseStorageConfigured()) {
    const supabase = getSupabaseClient();
    const storagePath = `tenants/${tenantId}/captured-records/${fileName}`;

    const { error } = await supabase.storage
      .from(storageBucket)
      .upload(storagePath, file.buffer, {
        contentType: file.mimetype,
        upsert: false,
      });

    if (error) {
      throw new Error(`Supabase image upload failed: ${error.message}`);
    }

    return {
      imageUrl: null,
      storagePath,
    };
  }

  const localFilePath = path.join(localUploadFolder, fileName);

  await fs.promises.writeFile(localFilePath, file.buffer);

  return {
    imageUrl: `/uploads/captured-images/${fileName}`,
    storagePath: localFilePath.replace(/\\/g, '/'),
  };
}

function formatCapturedRecord(record) {
  return {
    id: record.id,
    tenant_id: record.tenant_id,
    customer_id: record.customer_id,
    location_id: record.location_id,
    category_id: record.category_id,
    customer_name: record.customers?.name || null,
    location_name: record.locations?.name || null,
    category_name: record.categories?.name || null,
    description: record.description,
    latitude: record.latitude,
    longitude: record.longitude,
    image_url: record.image_url,
    image_path: record.image_path,
    captured_at: record.captured_at,
    received_at: record.received_at,
    created_at: record.created_at,
    updated_at: record.updated_at,
  };
}

async function getCapturedRecordById(recordId, tenantId) {
  return prisma.captured_records.findFirst({
    where: {
      id: recordId,
      tenant_id: tenantId,
    },
    include: {
      customers: {
        select: {
          name: true,
        },
      },
      locations: {
        select: {
          name: true,
        },
      },
      categories: {
        select: {
          name: true,
        },
      },
    },
  });
}

async function attachImagesToRecords(req, records) {
  if (records.length === 0) {
    return records;
  }

  const recordIds = records.map((record) => record.id);

  const images = await prisma.captured_images.findMany({
    where: {
      captured_record_id: {
        in: recordIds,
      },
      tenant_id: req.user.tenantId,
    },
    orderBy: {
      id: 'asc',
    },
  });

  const imagesByRecordId = new Map();

  for (const image of images) {
    const fullImageUrl = await resolveImageUrl(
      req,
      image.image_url,
      image.storage_path
    );

    const currentImages = imagesByRecordId.get(image.captured_record_id) || [];

    currentImages.push({
      ...image,
      image_url: isSupabaseStoragePath(image.storage_path)
        ? null
        : image.image_url,
      full_image_url: fullImageUrl,
    });

    imagesByRecordId.set(image.captured_record_id, currentImages);
  }

  return Promise.all(
    records.map(async (record) => {
      const recordImages = imagesByRecordId.get(record.id) || [];

      const fullImageUrl =
        recordImages[0]?.full_image_url ||
        (await resolveImageUrl(req, record.image_url, record.image_path));

      return {
        ...record,
        image_url: isSupabaseStoragePath(record.image_path)
          ? null
          : record.image_url,
        images: recordImages,
        full_image_url: fullImageUrl,
      };
    })
  );
}

router.get('/', async (req, res) => {
  try {
    const records = await prisma.captured_records.findMany({
      where: {
        tenant_id: req.user.tenantId,
      },
      include: {
        customers: {
          select: {
            name: true,
          },
        },
        locations: {
          select: {
            name: true,
          },
        },
        categories: {
          select: {
            name: true,
          },
        },
      },
      orderBy: {
        received_at: 'desc',
      },
    });

    const formattedRecords = records.map(formatCapturedRecord);
    const recordsWithImages = await attachImagesToRecords(req, formattedRecords);

    res.json(recordsWithImages);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch captured records',
      error: error.message,
    });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const recordId = parsePositiveInteger(req.params.id);

    if (!recordId) {
      return res.status(400).json({
        message: 'Invalid captured record ID',
      });
    }

    const record = await getCapturedRecordById(recordId, req.user.tenantId);

    if (!record) {
      return res.status(404).json({
        message: 'Captured record not found',
      });
    }

    const formattedRecord = formatCapturedRecord(record);
    const recordsWithImages = await attachImagesToRecords(req, [formattedRecord]);

    res.json(recordsWithImages[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch captured record',
      error: error.message,
    });
  }
});

router.post(
  '/',
  upload.fields([
    { name: 'images', maxCount: 10 },
    { name: 'image', maxCount: 1 },
  ]),
  async (req, res) => {
    try {
      const {
        customer_id,
        location_id,
        category_id,
        description,
        latitude,
        longitude,
        captured_at,
      } = req.body;

      const customerId = parsePositiveInteger(customer_id);
      const locationId = parsePositiveInteger(location_id);
      const categoryId = parsePositiveInteger(category_id);

      if (!customerId || !locationId || !categoryId) {
        return res.status(400).json({
          message: 'Customer, location and category are required',
        });
      }

      const latitudeValue = toOptionalDecimalValue(latitude);
      const longitudeValue = toOptionalDecimalValue(longitude);

      if (latitudeValue === undefined || longitudeValue === undefined) {
        return res.status(400).json({
          message: 'Latitude and longitude must be valid numbers',
        });
      }

      const capturedAtValue = captured_at
        ? new Date(captured_at)
        : new Date();

      if (Number.isNaN(capturedAtValue.getTime())) {
        return res.status(400).json({
          message: 'Captured date/time is invalid',
        });
      }

      const [customer, location, category] = await Promise.all([
        prisma.customers.findFirst({
          where: {
            id: customerId,
            tenant_id: req.user.tenantId,
          },
        }),
        prisma.locations.findFirst({
          where: {
            id: locationId,
            tenant_id: req.user.tenantId,
          },
        }),
        prisma.categories.findFirst({
          where: {
            id: categoryId,
            tenant_id: req.user.tenantId,
          },
        }),
      ]);

      if (!customer || !location || !category) {
        return res.status(400).json({
          message:
            'Selected customer, location, or category does not belong to your tenant',
        });
      }

      const uploadedFiles = getUploadedImageFiles(req);
      const uploadedImages = [];

      for (const file of uploadedFiles) {
        const uploadedImage = await uploadImageFile(file, req.user.tenantId);
        uploadedImages.push(uploadedImage);
      }

      const primaryImage =
        uploadedImages.length > 0 ? uploadedImages[0] : null;

      const imageUrl = primaryImage ? primaryImage.imageUrl : null;
      const imagePath = primaryImage ? primaryImage.storagePath : null;

      const capturedRecord = await prisma.$transaction(async (tx) => {
        const record = await tx.captured_records.create({
          data: {
            tenant_id: req.user.tenantId,
            customer_id: customerId,
            location_id: locationId,
            category_id: categoryId,
            description: description || '',
            latitude: latitudeValue,
            longitude: longitudeValue,
            image_url: imageUrl,
            image_path: imagePath,
            captured_at: capturedAtValue,
          },
        });

        if (uploadedImages.length > 0) {
          await tx.captured_images.createMany({
            data: uploadedImages.map((image) => ({
              captured_record_id: record.id,
              tenant_id: req.user.tenantId,
              image_url: image.imageUrl,
              storage_path: image.storagePath,
            })),
          });
        }

        return record;
      });

      const savedRecord = await getCapturedRecordById(
        capturedRecord.id,
        req.user.tenantId
      );

      const formattedRecord = formatCapturedRecord(savedRecord);
      const recordsWithImages = await attachImagesToRecords(req, [formattedRecord]);

      res.status(201).json({
        message: 'Captured record created successfully',
        record: recordsWithImages[0],
      });
    } catch (error) {
      res.status(500).json({
        message: 'Failed to create captured record',
        error: error.message,
      });
    }
  }
);

module.exports = router;