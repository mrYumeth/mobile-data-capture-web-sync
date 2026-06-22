const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');

const pool = require('../config/db');

const router = express.Router();

const uploadFolder = path.join(__dirname, '../../uploads/captured-images');

if (!fs.existsSync(uploadFolder)) {
  fs.mkdirSync(uploadFolder, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadFolder);
  },
  filename: (req, file, cb) => {
    const uniqueName = `${Date.now()}-${Math.round(
      Math.random() * 1e9
    )}${path.extname(file.originalname)}`;

    cb(null, uniqueName);
  },
});

const upload = multer({ storage });

function buildImageUrl(req, imageUrl) {
  if (!imageUrl) {
    return null;
  }

  return `${req.protocol}://${req.get('host')}${imageUrl}`;
}

function getUploadedImageFiles(req) {
  const multipleImages = req.files?.images || [];
  const legacyImage = req.files?.image || [];

  return [...multipleImages, ...legacyImage];
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

      const uploadedImages = getUploadedImageFiles(req);
      const primaryImage =
        uploadedImages.length > 0 ? uploadedImages[0] : null;

      const imageUrl = primaryImage
        ? `/uploads/captured-images/${primaryImage.filename}`
        : null;

      const imagePath = primaryImage
        ? primaryImage.path.replace(/\\/g, '/')
        : null;

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

      for (const file of uploadedImages) {
        const currentImageUrl = `/uploads/captured-images/${file.filename}`;
        const currentImagePath = file.path.replace(/\\/g, '/');

        await client.query(
          `
          INSERT INTO captured_images (
            captured_record_id,
            image_url,
            storage_path
          )
          VALUES ($1, $2, $3)
          `,
          [capturedRecord.id, currentImageUrl, currentImagePath]
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