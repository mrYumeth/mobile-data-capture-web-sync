import { useEffect, useState } from 'react';

function MasterDataPage({
  title,
  description,
  api,
  fields,
  emptyForm,
  tableColumns,
  itemLabel,
}) {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadItems();
  }, []);

  async function loadItems() {
    try {
      setLoading(true);
      setError('');
      const data = await api.getAll();
      setItems(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  function handleEdit(item) {
    const nextForm = {};

    fields.forEach((field) => {
      nextForm[field.name] = item[field.name] || '';
    });

    setForm(nextForm);
    setEditingId(item.id);
    setMessage('');
    setError('');
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const requiredField = fields.find(
      (field) => field.required && !form[field.name]?.trim()
    );

    if (requiredField) {
      setError(`${requiredField.label} is required.`);
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');

      if (editingId) {
        await api.update(editingId, form);
        setMessage(`${itemLabel} updated successfully.`);
      } else {
        await api.create(form);
        setMessage(`${itemLabel} created successfully.`);
      }

      resetForm();
      await loadItems();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(item) {
    const confirmed = window.confirm(
      `Are you sure you want to delete ${item.name}?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setError('');
      setMessage('');
      await api.remove(item.id);
      setMessage(`${itemLabel} deleted successfully.`);
      await loadItems();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
        <p className="text-gray-600">{description}</p>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      {message && (
        <div className="mb-4 rounded-lg bg-green-50 p-4 text-green-700">
          {message}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[380px_1fr]">
        <div className="app-card p-6">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            {editingId ? `Edit ${itemLabel}` : `Add ${itemLabel}`}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            {fields.map((field) => (
              <FormField
                key={field.name}
                field={field}
                value={form[field.name]}
                onChange={handleChange}
              />
            ))}

            <div className="flex gap-3">
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg bg-[#EB5979] px-4 py-2 font-medium text-white hover:bg-[#D94368] disabled:opacity-60"
              >
                {saving ? 'Saving...' : editingId ? 'Update' : 'Save'}
              </button>

              {editingId && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="rounded-lg border border-gray-300 px-4 py-2 font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        <div className="app-card p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              {itemLabel} List
            </h2>
            <button
              type="button"
              onClick={loadItems}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Refresh
            </button>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : items.length === 0 ? (
            <p className="text-gray-500">No records found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left text-sm">
                <thead>
                  <tr className="border-b bg-gray-50">
                    {tableColumns.map((column) => (
                      <TableHeader key={column.key}>{column.label}</TableHeader>
                    ))}
                    <TableHeader>Actions</TableHeader>
                  </tr>
                </thead>

                <tbody>
                  {items.map((item) => (
                    <tr key={item.id} className="border-b">
                      {tableColumns.map((column) => (
                        <TableCell key={column.key}>
                          {item[column.key] || '-'}
                        </TableCell>
                      ))}
                      <TableCell>
                        <div className="flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleEdit(item)}
                            className="rounded bg-amber-100 px-3 py-1 text-amber-700 hover:bg-amber-200"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(item)}
                            className="rounded bg-red-100 px-3 py-1 text-red-700 hover:bg-red-200"
                          >
                            Delete
                          </button>
                        </div>
                      </TableCell>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function FormField({ field, value, onChange }) {
  if (field.type === 'textarea') {
    return (
      <div>
        <label className="mb-1 block text-sm font-medium text-gray-700">
          {field.label}
        </label>
        <textarea
          name={field.name}
          value={value || ''}
          onChange={onChange}
          required={field.required}
          rows="3"
          className="form-input"
        />
      </div>
    );
  }

  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-gray-700">
        {field.label}
      </label>
      <input
        type={field.type || 'text'}
        name={field.name}
        value={value || ''}
        onChange={onChange}
        required={field.required}
        className="form-input"
      />
    </div>
  );
}

function TableHeader({ children }) {
  return (
    <th className="whitespace-nowrap px-4 py-3 font-semibold text-gray-700">
      {children}
    </th>
  );
}

function TableCell({ children }) {
  return <td className="px-4 py-3 text-gray-700">{children}</td>;
}

export default MasterDataPage;