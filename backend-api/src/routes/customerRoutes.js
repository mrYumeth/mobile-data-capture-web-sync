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

function isValidPhoneNumber(phone) {
  if (!phone) {
    return true;
  }

  return /^\d{10}$/.test(phone);
}

router.get('/', async (req, res) => {
  try {
    const customers = await prisma.customers.findMany({
      where: {
        tenant_id: req.user.tenantId,
      },
      orderBy: {
        id: 'desc',
      },
    });

    res.json(customers);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch customers',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { name, phone, email, address } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Customer name is required',
      });
    }

    if (!isValidPhoneNumber(phone)) {
      return res.status(400).json({
        message: 'Phone number must contain exactly 10 digits',
      });
    }

    const customer = await prisma.customers.create({
      data: {
        tenant_id: req.user.tenantId,
        name: name.trim(),
        phone: phone || null,
        email: email || null,
        address: address || null,
      },
    });

    res.status(201).json(customer);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create customer',
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
    const { name, phone, email, address } = req.body;
    const customerId = Number(id);

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Customer name is required',
      });
    }

    if (!isValidPhoneNumber(phone)) {
      return res.status(400).json({
        message: 'Phone number must contain exactly 10 digits',
      });
    }

    const existingCustomer = await prisma.customers.findFirst({
      where: {
        id: customerId,
        tenant_id: req.user.tenantId,
      },
    });

    if (!existingCustomer) {
      return res.status(404).json({
        message: 'Customer not found',
      });
    }

    const updatedCustomer = await prisma.customers.update({
      where: {
        id: customerId,
      },
      data: {
        name: name.trim(),
        phone: phone || null,
        email: email || null,
        address: address || null,
        updated_at: new Date(),
      },
    });

    res.json(updatedCustomer);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update customer',
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
    const customerId = Number(id);

    const existingCustomer = await prisma.customers.findFirst({
      where: {
        id: customerId,
        tenant_id: req.user.tenantId,
      },
    });

    if (!existingCustomer) {
      return res.status(404).json({
        message: 'Customer not found',
      });
    }

    const deletedCustomer = await prisma.customers.delete({
      where: {
        id: customerId,
      },
    });

    res.json({
      message: 'Customer deleted successfully',
      deletedCustomer,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete customer',
      error: error.message,
    });
  }
});

module.exports = router;