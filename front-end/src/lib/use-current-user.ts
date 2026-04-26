"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface AppUser {
  id:       string;
  name:     string;
  initials: string;
}

export const USERS: AppUser[] = [
  { id: "kys", name: "김영선", initials: "KY" },
  { id: "pjh", name: "박지훈", initials: "JH" },
];

interface UserStore {
  currentUserId: string;
  setCurrentUser: (id: string) => void;
}

export const useCurrentUser = create<UserStore>()(
  persist(
    (set) => ({
      currentUserId:  "kys",
      setCurrentUser: (id) => set({ currentUserId: id }),
    }),
    { name: "fms.currentUser" }
  )
);

export function getCurrentUser(): AppUser {
  const id = useCurrentUser.getState().currentUserId;
  return USERS.find(u => u.id === id) ?? USERS[0];
}
