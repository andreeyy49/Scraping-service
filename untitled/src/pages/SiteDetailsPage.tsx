import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
    Box,
    Container,
    Typography,
    Card,
    CardContent,
    CircularProgress
} from "@mui/material";
import { getSiteDetails } from "../api/siteApi";

export default function SiteDetailsPage() {
    const { id } = useParams();
    const [site, setSite] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchSiteDetails = async () => {
            try {
                const data = await getSiteDetails(id!);
                setSite(data);
            } catch (err) {
                console.error("Ошибка при загрузке деталей сайта", err);
            } finally {
                setLoading(false);
            }
        };

        fetchSiteDetails();
    }, [id]);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    if (!site) {
        return (
            <Container sx={{ mt: 8 }}>
                <Typography variant="h6">Сайт не найден</Typography>
            </Container>
        );
    }

    return (
        <Container sx={{ mt: 8 }}>
            <Typography variant="h4" gutterBottom>
                👁️ Детали сайта
            </Typography>

            <Card sx={{ mb: 4 }}>
                <CardContent>
                    <Typography variant="h6">{site.name || site.url}</Typography>
                    <Typography>URL: {site.url}</Typography>
                    <Typography>
                        Последний скрапинг:{" "}
                        {new Date(site.lastScraped).toLocaleString()}
                    </Typography>
                    <Typography>Кол-во страниц: {site.pagesCount}</Typography>
                    {/* Можем добавить и другие данные */}
                </CardContent>
            </Card>

            {/* Пример: список страниц или товаров */}
            {site.pages && site.pages.length > 0 && (
                <>
                    <Typography variant="h6" gutterBottom>
                        🧾 Страницы сайта:
                    </Typography>
                    <Box component="ul">
                        {site.pages.map((page: any) => (
                            <li key={page.id}>
                                <Typography>{page.title} ({page.url})</Typography>
                            </li>
                        ))}
                    </Box>
                </>
            )}
        </Container>
    );
}
