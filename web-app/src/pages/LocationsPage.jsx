import MasterDataPage from './MasterDataPage'
import { locationApi } from '../services/api'

const PREVIEW_LOCATIONS = [
  {
    id: 'mock-location-1',
    name: 'Colombo Central',
    address: 'Colombo 03, Western Province',
  },
  {
    id: 'mock-location-2',
    name: 'Galle Regional Office',
    address: 'Galle, Southern Province',
  },
  {
    id: 'mock-location-3',
    name: 'Kandy Field Point',
    address: 'Kandy, Central Province',
  },
  {
    id: 'mock-location-4',
    name: 'Jaffna Service Area',
    address: 'Jaffna, Northern Province',
  },
]

function LocationsPage() {
  return (
    <MasterDataPage
      title="Location"
      description="Manage field locations used by the mobile data capture app."
      api={locationApi}
      itemLabel="Location"
      previewItems={PREVIEW_LOCATIONS}
      emptyForm={{
        name: '',
        address: '',
      }}
      fields={[
        {
          name: 'name',
          label: 'Location Name',
          required: true,
        },
        {
          name: 'address',
          label: 'Address',
          type: 'textarea',
        },
      ]}
      tableColumns={[
        {
          key: 'name',
          label: 'Location Name',
        },
        {
          key: 'address',
          label: 'Address',
        },
      ]}
    />
  )
}

export default LocationsPage
