import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subject, interval } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { PaymentResponse } from '../../models/payment.model';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  payment?: PaymentResponse;
  iframeUrl?: SafeResourceUrl;
  loading = true;
  error?: string;

  paymentCompleted = false;
  paymentSuccess = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sanitizer: DomSanitizer,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    const paymentId = this.route.snapshot.paramMap.get('paymentId');
    if (paymentId) {
      this.loadPayment(+paymentId);
    } else {
      this.error = 'Paiement non spécifié';
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPayment(id: number): void {
    this.paymentService.getPaymentById(id).subscribe({
      next: (payment) => {
        this.payment = payment;

        if (payment.status === 'SUCCESS' || payment.status === 'FAILED' || payment.status === 'CANCELLED') {
          this.onPaymentCompleted(payment);
        } else {
          this.iframeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(payment.pleenkPaymentUrl);
          this.startPolling(id);
        }

        this.loading = false;
      },
      error: () => {
        this.error = 'Paiement introuvable';
        this.loading = false;
      }
    });
  }

  startPolling(paymentId: number): void {
    interval(3000)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.paymentService.getPaymentById(paymentId))
      )
      .subscribe({
        next: (payment) => {
          this.payment = payment;
          if (payment.status === 'SUCCESS' || payment.status === 'FAILED' || payment.status === 'CANCELLED') {
            this.onPaymentCompleted(payment);
          }
        },
        error: (err) => console.error('Erreur polling:', err)
      });
  }

  onPaymentCompleted(payment: PaymentResponse): void {
    this.destroy$.next();
    this.paymentCompleted = true;
    this.paymentSuccess = payment.status === 'SUCCESS';
    this.iframeUrl = undefined;
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
