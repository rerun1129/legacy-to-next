import { TruckBLEntry } from "@/components/fms/truck-bl/truck-bl-entry";

interface Props {
  params: Promise<{ id: string }>;
}

export default async function TruckBLEntryEditPage({ params }: Props) {
  const { id: idStr } = await params;
  const id = Number(idStr);
  return <TruckBLEntry id={isNaN(id) ? undefined : id} />;
}
