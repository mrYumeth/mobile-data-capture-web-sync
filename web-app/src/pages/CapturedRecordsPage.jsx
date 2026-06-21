import { useEffect, useState } from 'react';
import { capturedRecordApi } from '../services/api';

function CapturedRecordsPage() {
  const [records, setRecords] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadRecords();
  }, []);

  async function loadRecords() {
    try {
      setLoading(true);
      setError('');
      const data = await capturedRecordApi.getAll();
      setRecords(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function openDetails(recordId) {
    try {
      setDetailsLoading(true);
      setError('');
      const data = await capturedRecordApi.getById(recordId);
      setSelectedRecord(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setDetailsLoading(false);
    }
  }

  function closeDetails() {
    setSelectedRecord(null);
  }

  function formatDateTime(value) {
    if (!value) {
      return '-';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleString();
  }

  return (
    <div>
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Captured Records
          </h1>
          <p className="text-gray-600">
            View field records uploaded from the mobile app.
          </p>
        </div>

        <button
          type="button"
          onClick={loadRecords}
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 font-medium text-gray-700 hover:bg-gray-50"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[1fr_420px]">
        <div className="app-card p-6">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            Uploaded Records List
          </h2>

          {loading ? (
            <p>Loading captured records...</p>
          ) : records.length === 0 ? (
            <div className="rounded-lg border border-dashed border-gray-300 p-8 text-center">
              <p className="font-medium text-gray-700">
                No captured records found.
              </p>
              <p className="mt-1 text-sm text-gray-500">
                Records uploaded from the mobile app will appear here.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left text-sm">
                <thead>
                  <tr className="border-b bg-gray-50">
                    <TableHeader>ID</TableHeader>
                    <TableHeader>Customer</TableHeader>
                    <TableHeader>Location</TableHeader>
                    <TableHeader>Category</TableHeader>
                    <TableHeader>Captured At</TableHeader>
                    <TableHeader>Received At</TableHeader>
                    <TableHeader>Action</TableHeader>
                  </tr>
                </thead>

                <tbody>
                  {records.map((record) => (
                    <tr key={record.id} className="border-b hover:bg-gray-50">
                      <TableCell>#{record.id}</TableCell>
                      <TableCell>{record.customer_name || '-'}</TableCell>
                      <TableCell>{record.location_name || '-'}</TableCell>
                      <TableCell>{record.category_name || '-'}</TableCell>
                      <TableCell>{formatDateTime(record.captured_at)}</TableCell>
                      <TableCell>{formatDateTime(record.received_at)}</TableCell>
                      <TableCell>
                        <button
                          type="button"
                          onClick={() => openDetails(record.id)}
                          className="rounded bg-[#2563EB] px-3 py-1 text-white hover:bg-[#1D4ED8]"
                        >
                          View
                        </button>
                      </TableCell>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="app-card p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Record Details
            </h2>

            {selectedRecord && (
              <button
                type="button"
                onClick={closeDetails}
                className="text-sm font-medium text-gray-500 hover:text-gray-700"
              >
                Clear
              </button>
            )}
          </div>

          {detailsLoading ? (
            <p>Loading details...</p>
          ) : selectedRecord ? (
            <RecordDetails
              record={selectedRecord}
              formatDateTime={formatDateTime}
            />
          ) : (
            <div className="rounded-lg border border-dashed border-gray-300 p-8 text-center">
              <p className="font-medium text-gray-700">
                Select a record to view details.
              </p>
              <p className="mt-1 text-sm text-gray-500">
                The selected record’s GPS, image, and captured data will appear
                here.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function RecordDetails({ record, formatDateTime }) {
  const imageUrl = record.full_image_url;

  return (
    <div className="space-y-4">
      <div className="rounded-lg bg-slate-50 p-4">
        <p className="text-sm font-medium text-gray-500">Record ID</p>
        <p className="text-xl font-bold text-gray-900">#{record.id}</p>
      </div>

      <DetailSection title="Master Data">
        <DetailRow label="Customer" value={record.customer_name} />
        <DetailRow label="Location" value={record.location_name} />
        <DetailRow label="Category" value={record.category_name} />
      </DetailSection>

      <DetailSection title="Captured Information">
        <DetailRow
          label="Captured At"
          value={formatDateTime(record.captured_at)}
        />
        <DetailRow
          label="Received At"
          value={formatDateTime(record.received_at)}
        />
        <DetailRow label="Description" value={record.description} />
      </DetailSection>

      <DetailSection title="GPS Coordinates">
        <DetailRow label="Latitude" value={record.latitude} />
        <DetailRow label="Longitude" value={record.longitude} />
      </DetailSection>

      <div>
        <h3 className="mb-2 text-sm font-bold uppercase tracking-wide text-gray-500">
          Captured Image
        </h3>

        {imageUrl ? (
          <a href={imageUrl} target="_blank" rel="noreferrer">
            <img
              src={imageUrl}
              alt="Captured field record"
              className="h-64 w-full rounded-xl border border-gray-200 object-cover"
            />
          </a>
        ) : (
          <div className="rounded-lg border border-dashed border-gray-300 p-6 text-center text-gray-500">
            No image available.
          </div>
        )}
      </div>
    </div>
  );
}

function DetailSection({ title, children }) {
  return (
    <div>
      <h3 className="mb-2 text-sm font-bold uppercase tracking-wide text-gray-500">
        {title}
      </h3>
      <div className="rounded-lg border border-gray-200">
        {children}
      </div>
    </div>
  );
}

function DetailRow({ label, value }) {
  return (
    <div className="grid grid-cols-[120px_1fr] border-b border-gray-200 px-4 py-3 last:border-b-0">
      <span className="font-medium text-gray-600">{label}</span>
      <span className="text-gray-900">{value || '-'}</span>
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
  return <td className="whitespace-nowrap px-4 py-3 text-gray-700">{children}</td>;
}

export default CapturedRecordsPage;