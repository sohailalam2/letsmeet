export class FaultTolerance {
 public static async retry<T>(
    fn: () => Promise<T>,
    maxAttempts: number = 10,
    delayMs: number = 1000
  ): Promise<T | null> {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        const response = await fn();
        console.log(`✅ Success on attempt ${attempt}`);
        return response;
      } catch (error) {
        console.warn(`❌ Attempt ${attempt} failed`, error);
        if (attempt < maxAttempts) {
          await new Promise((resolve) => setTimeout(resolve, delayMs));
        }
      }
    }

    console.error(`🚫 All ${maxAttempts} attempts failed.`);
    return null;
  }
}
