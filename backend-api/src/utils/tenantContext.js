async function runWithTenant(prisma, tenantId, callback) {
  if (!tenantId) {
    throw new Error('Tenant ID is required');
  }

  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      'SELECT set_config($1, $2, true)',
      'app.current_tenant_id',
      tenantId.toString()
    );

    return callback(tx);
  });
}

module.exports = {
  runWithTenant,
};