import MasterDataPage from './MasterDataPage';
import { categoryApi } from '../services/api';

function CategoriesPage() {
  return (
    <MasterDataPage
      title="Category"
      description="Manage capture categories used when recording field data."
      api={categoryApi}
      itemLabel="Category"
      emptyForm={{
        name: '',
        description: '',
      }}
      fields={[
        {
          name: 'name',
          label: 'Category Name',
          required: true,
        },
        {
          name: 'description',
          label: 'Description',
          type: 'textarea',
        },
      ]}
      tableColumns={[
        {
          key: 'name',
          label: 'Category Name',
        },
        {
          key: 'description',
          label: 'Description',
        },
      ]}
    />
  );
}

export default CategoriesPage;