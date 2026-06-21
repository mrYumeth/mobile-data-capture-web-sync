import MasterDataPage from './MasterDataPage';
import { locationApi } from '../services/api';

function LocationsPage() {
  return (
    <MasterDataPage
      title="Location"
      description="Manage field locations used by the mobile data capture app."
      api={locationApi}
      itemLabel="Location"
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
  );
}

export default LocationsPage;