import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import {
    Container,
    Typography,
    CircularProgress,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Grid,
    Card,
    CardContent,
    CardMedia,
    Box,
} from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

interface Product {
    id: string;
    name: string;
    price: string;
    imageUrl: string;
    productUrl: string;
}

export default function SiteStatsPage() {
    const { siteId } = useParams();
    const [newProducts, setNewProducts] = useState<Product[]>([]);
    const [oldProducts, setOldProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const token = localStorage.getItem("token");

                const [newRes, oldRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findNew/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` }
                    }),
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findOld/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` }
                    })
                ]);

                setNewProducts(newRes.data);
                setOldProducts(oldRes.data);
            } catch (err) {
                console.error("Ошибка при загрузке статистики", err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [siteId]);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Container sx={{ mt: 8 }}>
            <Typography variant="h4" gutterBottom>
                📊 Статистика по сайту #{siteId}
            </Typography>

            <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h6">🆕 Новые товары ({newProducts.length})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <ProductGrid products={newProducts} />
                </AccordionDetails>
            </Accordion>

            <Accordion defaultExpanded sx={{ mt: 2 }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h6">📉 Пропавшие товары ({oldProducts.length})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <ProductGrid products={oldProducts} />
                </AccordionDetails>
            </Accordion>
        </Container>
    );
}

function ProductGrid({ products }: { products: Product[] }) {
    if (products.length === 0) {
        return <Typography>Нет данных</Typography>;
    }

    return (
        <Grid container spacing={3}>
            {products.map((product) => (
                <Grid item xs={12} sm={6} md={4} key={product.id}>
                    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <CardMedia
                            component="img"
                            height="200"
                            image={product.imageUrl || "https://via.placeholder.com/300"}
                            alt={product.name}
                            sx={{ objectFit: 'contain', p: 1 }}
                        />
                        <CardContent sx={{ flexGrow: 1 }}>
                            <Typography gutterBottom variant="h6">
                                {product.name}
                            </Typography>
                            <Typography variant="h5" color="primary" sx={{ fontWeight: 'bold' }}>
                                {product.price}
                            </Typography>
                        </CardContent>
                        <Box sx={{ p: 2 }}>
                            <a href={product.productUrl} target="_blank" rel="noopener noreferrer">
                                <button style={{ width: "100%" }}>Перейти к товару</button>
                            </a>
                        </Box>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}
