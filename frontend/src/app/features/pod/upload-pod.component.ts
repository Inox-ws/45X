import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { validateFile } from '../../shared/utils/file-validation';
import { PodService } from '../../core/services/pod.service';
import { PodUploadResponse } from '../../core/models/pod.model';

const ALLOWED_EXTENSIONS = ['.pdf', '.png', '.jpg'];
const MAX_SIZE_MB = 25;

@Component({
  selector: 'app-upload-pod',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatIconModule,
    PageHeaderComponent
  ],
  templateUrl: './upload-pod.component.html',
  styleUrl: './upload-pod.component.scss'
})
export class UploadPodComponent {
  readonly fileError = signal<string | null>(null);
  readonly submitError = signal<string | null>(null);
  readonly selectedFileName = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly result = signal<PodUploadResponse | null>(null);

  private selectedFile: File | null = null;

  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly podService: PodService
  ) {
    this.form = this.fb.group({
      invoiceId: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)])
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.fileError.set(null);
    this.selectedFile = null;
    this.selectedFileName.set(null);

    if (!file) {
      return;
    }
    const error = validateFile(file, ALLOWED_EXTENSIONS, MAX_SIZE_MB);
    if (error) {
      this.fileError.set(error);
      return;
    }
    this.selectedFile = file;
    this.selectedFileName.set(file.name);
  }

  async upload(): Promise<void> {
    if (this.form.invalid || !this.selectedFile) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.submitError.set(null);

    try {
      const response = await firstValueFrom(
        this.podService.uploadPod(this.selectedFile, this.form.getRawValue().invoiceId!)
      );
      this.result.set(response);
    } catch (error: any) {
      this.submitError.set(error?.error?.detail ?? 'Upload failed. Check the invoice ID and file, then try again.');
    } finally {
      this.submitting.set(false);
    }
  }

  startOver(): void {
    this.result.set(null);
    this.selectedFile = null;
    this.selectedFileName.set(null);
    this.submitError.set(null);
    this.form.reset();
  }
}
