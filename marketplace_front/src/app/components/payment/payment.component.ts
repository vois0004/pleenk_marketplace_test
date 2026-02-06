import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html'
})
export class PaymentComponent implements OnInit {
  product?: Product;
  quantity = 1;

  loading = true;
  creatingPayment = false;
  error?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('productId');
    if (productId) {
      this.loadProduct(+productId);
    } else {
      this.error = 'Produit non spécifié';
      this.loading = false;
    }
  }

  loadProduct(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: () => {
        this.error = 'Produit introuvable';
        this.loading = false;
      }
    });
  }

  get totalAmount(): number {
    return this.product ? this.product.price * this.quantity : 0;
  }

  createPayment(): void {
    if (!this.product) return;

    this.creatingPayment = true;
    this.error = undefined;

    this.paymentService.createPayment({
      productId: this.product.id,
      quantity: this.quantity
    }).subscribe({
      next: (payment) => {
        // Redirige vers la page de checkout avec l'iframe
        this.router.navigate(['/checkout', payment.id]);
      },
      error: (err) => {
        this.error = err.error?.detail || 'Erreur lors de la création du paiement';
        this.creatingPayment = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  incrementQuantity(): void {
    if (this.product && this.quantity < this.product.quantity) {
      this.quantity++;
    }
  }

  decrementQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }
}
