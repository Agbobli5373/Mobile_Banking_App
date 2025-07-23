import { z } from 'zod';

export const registerSchema = z.object({
    name: z
        .string()
        .min(1, 'Name is required')
        .min(2, 'Name must be at least 2 characters')
        .max(50, 'Name must be less than 50 characters')
        .regex(/^[a-zA-Z\s]+$/, 'Name must contain only letters and spaces'),

    phoneNumber: z
        .string()
        .min(1, 'Phone number is required')
        .transform((val) => val.replace(/\D/g, '')) // Remove non-digits first
        .pipe(z.string().regex(/^(233\d{9}|0\d{9})$/, 'Phone number must be a valid Ghana number (e.g. 0592063360 or 233592063360)')),

    pin: z
        .string()
        .min(1, 'PIN is required')
        .length(4, 'PIN must be exactly 4 digits')
        .regex(/^\d{4}$/, 'PIN must contain only digits'),

    confirmPin: z
        .string()
        .min(1, 'Please confirm your PIN')
        .length(4, 'PIN must be exactly 4 digits')
        .regex(/^\d{4}$/, 'PIN must contain only digits'),
}).refine((data) => data.pin === data.confirmPin, {
    message: "PINs don't match",
    path: ["confirmPin"],
});

export type RegisterFormData = z.infer<typeof registerSchema>;