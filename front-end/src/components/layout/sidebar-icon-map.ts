import type { LucideIcon } from "lucide-react";
import {
  KeyRound, UserCog, Building2, Megaphone, ShieldCheck,
  List, LayoutDashboard, FileText, Layers, Truck, Package,
  FilePlus, LayoutGrid, ChevronRight,
  Settings, Users, Globe, Bell, Lock, Database,
  BarChart, Clipboard, BookOpen, Tag, Folder,
  Coins, ArrowLeftRight, Box, Hash, Ship, Anchor,
} from "lucide-react";

export const ICON_MAP: Record<string, LucideIcon> = {
  KeyRound,
  UserCog,
  Building2,
  Megaphone,
  ShieldCheck,
  List,
  LayoutDashboard,
  FileText,
  Layers,
  Truck,
  Package,
  FilePlus,
  LayoutGrid,
  ChevronRight,
  Settings,
  Users,
  Globe,
  Bell,
  Lock,
  Database,
  BarChart,
  Clipboard,
  BookOpen,
  Tag,
  Folder,
  Coins,
  ArrowLeftRight,
  Box,
  Hash,
  Ship,
  Anchor,
};

export function resolveIcon(name: string | null | undefined): LucideIcon {
  return (name && ICON_MAP[name]) || List;
}
