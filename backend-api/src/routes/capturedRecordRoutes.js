const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const pool = require('../config/db');
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

if (!fs.existsSync(localUploadFolder)) {
  fs.mkdirSync(localUploadFolder, { recursive: true });
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024,
  },
});

function buildImageUrl(req, imageUrl) {
  if (!imageUrl) {
    return null;
  }

  if (/^https?:\/\//i.test(imageUrl)) {
    return imageUrl;
  }

  return `${req.protocol}://${req.get('host')}${imageUrl}`;
}

function getUploadedImageFiles(req) {
  const multipleImages = req.files?.images || [];
  const legacyImage = req.files?.image || [];

  return [...multipleImages, ...legacyImage];
}

async function uploadImageFile(file) {
  const extension = path.extname(file.originalname) || '.jpg';
  const fileName = `${Date.now()}-${crypto.randomUUID()}${extension}`;

  if (isSupabaseStorageConfigured()) {
    const supabase = getSupabaseClient();
    const storagePath = `captured-records/${fileName}`;

    const { error } = await supabase.storage
      .from(storageBucket)
      .upload(storagePath, file.buffer, {
        contentType: file.mimetype,
        upsert: false,
      });

    if (error) {
      throw new Error(`Supabase image upload failed: ${error.message}`);
    }

    const { data } = supabase.storage
      .from(storageBucket)
      .getPublicUrl(storagePath);

    return {
      imageUrl: data.publicUrl,
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

async function attachImagesToRecords(req, records) {
  if (records.length === 0) {
    return records;
  }

  const recordIds = records.map((record) => record.id);

  const imageResult = await pool.query(
    `
    SELECT
      id,
      captured_record_id,
      image_url,
      storage_path,
      created_at
    FROM captured_images
    WHERE captured_record_id = ANY($1::int[])
    ORDER BY id ASC
    `,
    [recordIds]
  );

  const imagesByRecordId = new Map();

  for (const image of imageResult.rows) {
    const currentImages = imagesByRecordId.get(image.captured_record_id) || [];

    currentImages.push({
      ...image,
      full_image_url: buildImageUrl(req, image.image_url),
    });

    imagesByRecordId.set(image.captured_record_id, currentImages);
  }

  return records.map((record) => {
    const images = imagesByRecordId.get(record.id) || [];

    return {
      ...record,
      images,
      full_image_url:
        images[0]?.full_image_url || buildImageUrl(req, record.image_url),
    };
  });
}

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_url,
        cr.image_path,
        cr.captured_at,
        cr.received_at,
        cr.created_at,
        cr.updated_at
      FROM captured_records cr
      LEFT JOIN customers c ON c.id = cr.customer_id
      LEFT JOIN locations l ON l.id = cr.location_id
      LEFT JOIN categories cat ON cat.id = cr.category_id
      ORDER BY cr.received_at DESC
    `);

    const records = await attachImagesToRecords(req, result.rows);

    res.json(records);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch captured records',
      error: error.message,
    });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      `
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_url,
        cr.image_path,
        cr.captured_at,
        cr.received_at,
        cr.created_at,
        cr.updated_at
      FROM captured_records cr
      LEFT JOIN customers c ON c.id = cr.customer_id
      LEFT JOIN locations l ON l.id = cr.location_id
      LEFT JOIN categories cat ON cat.id = cr.category_id
      WHERE cr.id = $1
      LIMIT 1
      `,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Captured record not found',
      });
    }

    const records = await attachImagesToRecords(req, [result.rows[0]]);

    res.json(records[0]);
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
    const client = await pool.connect();

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

      if (!customer_id || !location_id || !category_id) {
        return res.status(400).json({
          message: 'Customer, location and category are required',
        });
      }

      const uploadedFiles = getUploadedImageFiles(req);

      const uploadedImages = [];

      for (const file of uploadedFiles) {
        const uploadedImage = await uploadImageFile(file);
        uploadedImages.push(uploadedImage);
      }

      const primaryImage =
        uploadedImages.length > 0 ? uploadedImages[0] : null;

      const imageUrl = primaryImage ? primaryImage.imageUrl : null;
      const imagePath = primaryImage ? primaryImage.storagePath : null;

      await client.query('BEGIN');

      const recordResult = await client.query(
        `
        INSERT INTO captured_records (
          customer_id,
          location_id,
          category_id,
          description,
          latitude,
          longitude,
          image_url,
          image_path,
          captured_at
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
        RETURNING *
        `,
        [
          customer_id,
          location_id,
          category_id,
          description || '',
          latitude || null,
          longitude || null,
          imageUrl,
          imagePath,
          captured_at || new Date().toISOString(),
        ]
      );

      const capturedRecord = recordResult.rows[0];

      for (const image of uploadedImages) {
        await client.query(
          `
          INSERT INTO captured_images (
            captured_record_id,
            image_url,
            storage_path
          )
          VALUES ($1, $2, $3)
          `,
          [capturedRecord.id, image.imageUrl, image.storagePath]
        );
      }

      await client.query('COMMIT');

      const records = await attachImagesToRecords(req, [capturedRecord]);

      res.status(201).json({
        message: 'Captured record created successfully',
        record: records[0],
      });
    } catch (error) {
      await client.query('ROLLBACK');

      res.status(500).json({
        message: 'Failed to create captured record',
        error: error.message,
      });
    } finally {
      client.release();
    }
  }
);

module.exports = router;