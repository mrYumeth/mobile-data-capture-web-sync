const express = require('express');
const prisma = require('../config/prisma');

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
    const categories = await prisma.categories.findMany({
      where: {
        tenant_id: req.user.tenantId,
        is_active: true,
      },
      orderBy: {
        id: 'desc',
      },
    });

    res.json(categories);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch categories',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { name, description } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Category name is required',
      });
    }

    const category = await prisma.categories.create({
      data: {
        tenant_id: req.user.tenantId,
        name: name.trim(),
        description: description || null,
      },
    });

    res.status(201).json(category);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create category',
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
    const { name, description } = req.body;
    const categoryId = Number(id);

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Category name is required',
      });
    }

    if (!Number.isInteger(categoryId)) {
      return res.status(400).json({
        message: 'Invalid category ID',
      });
    }

    const existingCategory = await prisma.categories.findFirst({
      where: {
        id: categoryId,
        tenant_id: req.user.tenantId,
      },
    });

    if (!existingCategory) {
      return res.status(404).json({
        message: 'Category not found',
      });
    }

    const updatedCategory = await prisma.categories.update({
      where: {
        id: categoryId,
      },
      data: {
        name: name.trim(),
        description: description || null,
        updated_at: new Date(),
      },
    });

    res.json(updatedCategory);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update category',
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
    const categoryId = Number(id);

    if (!Number.isInteger(categoryId)) {
      return res.status(400).json({
        message: 'Invalid category ID',
      });
    }

    const existingCategory = await prisma.categories.findFirst({
      where: {
        id: categoryId,
        tenant_id: req.user.tenantId,
      },
    });

    if (!existingCategory) {
      return res.status(404).json({
        message: 'Category not found',
      });
    }

    const deletedCategory = await prisma.categories.delete({
      where: {
        id: categoryId,
      },
    });

    res.json({
      message: 'Category deleted successfully',
      deletedCategory,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete category',
      error: error.message,
    });
  }
});

module.exports = router;