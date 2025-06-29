export class DateTime {
    public static getFormattedCurrentDate(): string {
        const date = new Date(); // Always current date/time
        const month = date.toLocaleString("en-US", { month: "long" });
        const day = date.getDate();
        const year = date.getFullYear();

        const dayWithSuffix = DateTime.getDayWithSuffix(day);

        const time = date.toLocaleString("en-US", {
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
        });

        return `${month} ${dayWithSuffix}, ${year} | ${time}`;
    }

    private static getDayWithSuffix(day: number): string {
        if (day > 3 && day < 21) return `${day}th`;
        switch (day % 10) {
            case 1:
                return `${day}st`;
            case 2:
                return `${day}nd`;
            case 3:
                return `${day}rd`;
            default:
                return `${day}th`;
        }
    }
}
