export interface User {
    id: number,
    email: string,
    nickname: string,
    role: string,
    createdAt: Date,
    updatedAt: Date,
    lastLoginDate: Date,
    deletedAt: Date,
    emailVerified: boolean
}