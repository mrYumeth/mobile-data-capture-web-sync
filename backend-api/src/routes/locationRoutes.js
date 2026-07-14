const express = require('express');
const prisma = require('../config/prisma');
const { runWithTenant } = require('../utils/tenantContext');

const router = express.Router();

function requireAdmin(req, res) {
  if (req.user?.role !== 'admin') {
    res.status(403).json({
      message: 'Admin access is required',
    });
    return false;
  }

  return true;
}

router.get('/', async (req, res) => {
  try {
    const locations = await runWithTenant(
      prisma,
      req.user.tenantId,
      (tx) =>
        tx.locations.findMany({
          where: {
            tenant_id: req.user.tenantId,
            is_active: true,
          },
          orderBy: {
            id: 'desc',
          },
        })
    );

    res.json(locations);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch locations',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { name, address } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Location name is required',
      });
    }

    const location = await runWithTenant(
      prisma,
      req.user.tenantId,
      (tx) =>
        tx.locations.create({
          data: {
            tenant_id: req.user.tenantId,
            name: name.trim(),
            address: address || null,
          },
        })
    );

    res.status(201).json(location);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create location',
      error: error.message,
    });
  }
});

router.put('/:id', async (req, res) => {
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { id } = req.params;
    const { name, address } = req.body;
    const locationId = Number(id);

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Location name is required',
      });
    }

    if (!Number.isInteger(locationId)) {
      return res.status(400).json({
        message: 'Invalid location ID',
      });
    }

    const updatedLocation = await runWithTenant(
      prisma,
      req.user.tenantId,
      async (tx) => {
        const existingLocation = await tx.locations.findFirst({
          where: {
            id: locationId,
            tenant_id: req.user.tenantId,
          },
        });

        if (!existingLocation) {
          return null;
        }

        return tx.locations.update({
          where: {
            id: locationId,
          },
          data: {
            name: name.trim(),
            address: address || null,
            updated_at: new Date(),
          },
        });
      }
    );

    if (!updatedLocation) {
      return res.status(404).json({
        message: 'Location not found',
      });
    }

    res.json(updatedLocation);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update location',
      error: error.message,
    });
  }
});

router.delete('/:id', async (req, res) => {
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { id } = req.params;
    const locationId = Number(id);

    if (!Number.isInteger(locationId)) {
      return res.status(400).json({
        message: 'Invalid location ID',
      });
    }

    const deletedLocation = await runWithTenant(
      prisma,
      req.user.tenantId,
      async (tx) => {
        const existingLocation = await tx.locations.findFirst({
          where: {
            id: locationId,
            tenant_id: req.user.tenantId,
          },
        });

        if (!existingLocation) {
          return null;
        }

        return tx.locations.delete({
          where: {
            id: locationId,
          },
        });
      }
    );

    if (!deletedLocation) {
      return res.status(404).json({
        message: 'Location not found',
      });
    }

    res.json({
      message: 'Location deleted successfully',
      deletedLocation,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete location',
      error: error.message,
    });
  }
});

module.exports = router;