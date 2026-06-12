export type AttachmentBlKind = 'HOUSE' | 'MASTER' | 'TRUCK' | 'NON_BL';

export interface BlAttachment {
  id: number;
  blKind: AttachmentBlKind;
  blId: number;
  originalFilename: string;
  contentType: string | null;
  fileSize: number;
  uploadedBy: string;
  createdAt: string;
}
