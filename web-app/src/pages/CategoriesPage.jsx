import MasterDataPage from './MasterDataPage'
import { categoryApi } from '../services/api'

const PREVIEW_CATEGORIES = [
  {
    id: 'mock-category-1',
    name: 'Site Inspection',
    description: 'General field inspection records.',
  },
  {
    id: 'mock-category-2',
    name: 'Inventory Check',
    description: 'Stock and inventory verification records.',
  },
  {
    id: 'mock-category-3',
    name: 'Customer Visit',
    description: 'Customer visit and follow-up records.',
  },
  {
    id: 'mock-category-4',
    name: 'Maintenance',
    description: 'Equipment and site maintenance records.',
  },
]

function CategoriesPage() {
  return (
    <MasterDataPage
      title="Category"
      description="Manage capture categories used when recording field data."
      api={categoryApi}
      itemLabel="Category"
      previewItems={PREVIEW_CATEGORIES}
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
  )
}

export default CategoriesPage
