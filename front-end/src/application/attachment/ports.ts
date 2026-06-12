import type { AttachmentBlKind, BlAttachment } from '@/domain/attachment';

export interface BlAttachmentPort {
  list(blKind: AttachmentBlKind, blId: number): Promise<BlAttachment[]>;
  upload(blKind: AttachmentBlKind, blId: number, file: File): Promise<number>;
  download(id: number): Promise<Blob>;
  remove(id: number): Promise<void>;
}
