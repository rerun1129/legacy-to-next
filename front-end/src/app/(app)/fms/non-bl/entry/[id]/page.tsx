import { NonBLEntry } from "@/components/fms/non-bl/non-bl-entry";

interface Props {
  params: Promise<{ id: string }>;
}

export default async function NonBLEntryEditPage({ params }: Props) {
  const { id: idStr } = await params;
  const id = Number(idStr);
  return <NonBLEntry id={isNaN(id) ? undefined : id} />;
}
