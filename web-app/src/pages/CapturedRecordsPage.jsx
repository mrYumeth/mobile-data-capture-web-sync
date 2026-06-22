import { useEffect, useState } from 'react';
import { capturedRecordApi } from '../services/api';

function CapturedRecordsPage() {
  const [records, setRecords] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [isFullScreenOpen, setIsFullScreenOpen] = useState(false);
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
      setIsFullScreenOpen(false);

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
    setIsFullScreenOpen(false);
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
    <div className="min-w-0">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="min-w-0">
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
          className="w-fit rounded-lg border border-gray-300 bg-white px-4 py-2 font-medium text-gray-700 hover:bg-gray-50"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      <div className="grid min-w-0 grid-cols-1 gap-6 2xl:grid-cols-[minmax(0,1fr)_420px]">
        <div className="app-card min-w-0 p-6">
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
            <div className="w-full overflow-x-auto">
              <table className="w-full min-w-[760px] border-collapse text-left text-sm">
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
                          className="rounded bg-[#EB5979] px-3 py-1 text-white hover:bg-[#D94368]"
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

        <div className="app-card min-w-0 p-6">
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Record Details
            </h2>

            {selectedRecord && (
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setIsFullScreenOpen(true)}
                  className="rounded-lg bg-[#EB5979] px-3 py-2 text-sm font-semibold text-white transition hover:bg-[#D94368]"
                >
                  Full Screen
                </button>

                <button
                  type="button"
                  onClick={closeDetails}
                  className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-semibold text-gray-600 transition hover:bg-gray-100"
                >
                  Clear
                </button>
              </div>
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

      {isFullScreenOpen && selectedRecord && (
        <FullScreenRecordModal
          record={selectedRecord}
          formatDateTime={formatDateTime}
          onClose={() => setIsFullScreenOpen(false)}
        />
      )}
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
              className="max-h-72 w-full rounded-xl border border-gray-200 object-cover"
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

function FullScreenRecordModal({ record, formatDateTime, onClose }) {
  const imageUrl = record.full_image_url;

  return (
    <div className="fixed inset-0 z-[999] flex items-center justify-center bg-black/70 px-4 py-6 backdrop-blur-sm">
      <div className="max-h-[92vh] w-full max-w-6xl overflow-hidden rounded-[28px] bg-[#F1F1F3] shadow-[0_30px_100px_rgba(0,0,0,0.45)]">
        <div className="flex flex-col gap-4 border-b border-gray-300 px-6 py-5 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
              Captured Record
            </p>
            <h2 className="mt-1 text-2xl font-extrabold text-gray-950">
              Record #{record.id}
            </h2>
            <p className="text-sm text-gray-600">
              Full screen view of uploaded field record details.
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-full bg-[#EB5979] px-5 py-2 font-semibold text-white transition hover:bg-[#D94368]"
          >
            Close
          </button>
        </div>

        <div className="max-h-[calc(92vh-110px)] overflow-y-auto p-6">
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_420px]">
            <div className="space-y-5">
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
            </div>

            <div>
              <h3 className="mb-3 text-sm font-bold uppercase tracking-wide text-gray-500">
                Captured Image
              </h3>

              {imageUrl ? (
                <a href={imageUrl} target="_blank" rel="noreferrer">
                  <img
                    src={imageUrl}
                    alt="Captured field record"
                    className="max-h-[560px] w-full rounded-2xl border border-gray-300 object-cover shadow-lg"
                  />
                </a>
              ) : (
                <div className="flex min-h-[300px] items-center justify-center rounded-2xl border border-dashed border-gray-400 bg-white/70 p-6 text-center text-gray-500">
                  No image available.
                </div>
              )}

              <div className="mt-4 rounded-2xl bg-white/80 p-4 text-sm text-gray-600">
                Click the image to open it in a new browser tab.
              </div>
            </div>
          </div>
        </div>
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
      <div className="overflow-hidden rounded-lg border border-gray-200">
        {children}
      </div>
    </div>
  );
}

function DetailRow({ label, value }) {
  return (
    <div className="grid grid-cols-1 gap-1 border-b border-gray-200 px-4 py-3 last:border-b-0 sm:grid-cols-[130px_1fr]">
      <span className="font-medium text-gray-600">{label}</span>
      <span className="min-w-0 break-words text-gray-900">
        {value || '-'}
      </span>
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
  return (
    <td className="px-4 py-3 text-gray-700">
      <div className="max-w-[180px] truncate">{children}</div>
    </td>
  );
}

export default CapturedRecordsPage;