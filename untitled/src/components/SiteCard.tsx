import { useState } from "react";
import {
    Card,
    Typography,
    Button,
    Stack,
    Collapse,
    Divider,
    Chip,
} from "@mui/material";

interface Site {
    id: string;
    url: string;
    name: string;
    lastScraped: string;
    pageCount: number;
    status: "INDEXING" | "INDEXED" | "FAILED";
}

interface SiteCardProps {
    site: Site;
    onRescrape: () => void;
}

export default function SiteCard({ site, onRescrape }: SiteCardProps) {
    const [expanded, setExpanded] = useState(false);

    const handleToggle = () => {
        setExpanded((prev) => !prev);
    };

    const formatDate = (isoString: string): string => {
        const date = new Date(isoString);
        return date.toLocaleString("ru-RU", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    const getStatusColor = (status: Site["status"]) => {
        switch (status) {
            case "INDEXED":
                return "success";
            case "INDEXING":
                return "warning";
            case "FAILED":
                return "error";
            default:
                return "default";
        }
    };

    return (
        <Card
            sx={{
                mb: 1.5,
                px: 2,
                py: 1.5,
                boxShadow: 1,
                borderRadius: 2,
                backgroundColor: "#f9f9f9",
            }}
        >
            <Typography variant="h6" sx={{ mb: 0.5 }}>
                {site.name}
            </Typography>

            <Chip
                label={site.status}
                color={getStatusColor(site.status)}
                size="small"
                sx={{ mb: 1 }}
            />

            <Stack direction="row" spacing={1} justifyContent="flex-end" mt={0.5}>
                <Button size="small" variant="outlined" onClick={handleToggle}>
                    {expanded ? "Скрыть" : "Подробнее"}
                </Button>
                <Button size="small" variant="contained" onClick={onRescrape}>
                    Перескрапить
                </Button>
            </Stack>

            <Collapse in={expanded}>
                <Divider sx={{ my: 1.5 }} />
                <Typography variant="body2" color="text.secondary">
                    URL: {site.url}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Дата последнего скрапинга: {formatDate(site.lastScraped)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Страниц собрано: {site.pageCount}
                </Typography>
            </Collapse>
        </Card>
    );
}
