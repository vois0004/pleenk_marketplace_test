export interface CreatePaymentRequest {
  productId: number;
  quantity: number;
}

export interface PaymentResponse {
  id: number;
  amount: number;
  productId: number;
  quantity: number;
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';
  pleenkPaymentId: string;
  pleenkPaymentUrl: string;
  createdAt: string;
  updatedAt: string;
}
