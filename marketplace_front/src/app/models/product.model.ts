export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  sellerId: number;
  imageUrl: string;
  status: 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK';
}
