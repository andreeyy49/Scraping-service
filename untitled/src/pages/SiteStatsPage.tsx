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
    Pagination,
} from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

interface Product {
    id: string;
    name: string;
    price: string;
    imageUrl: string;
    productUrl: string;
}

interface PageData<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

export default function SiteStatsPage() {
    const { siteId } = useParams();
    const [newProducts, setNewProducts] = useState<PageData<Product> | null>(null);
    const [oldProducts, setOldProducts] = useState<PageData<Product> | null>(null);
    const [loading, setLoading] = useState(true);

    const [newPage, setNewPage] = useState(1);
    const [oldPage, setOldPage] = useState(1);
    const itemsPerPage = 6;

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const token = localStorage.getItem("token");

                const [newRes, oldRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findNew/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: newPage - 1, size: itemsPerPage }
                    }),
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findOld/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: oldPage - 1, size: itemsPerPage }
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
    }, [siteId, newPage, oldPage]);

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
                    <Typography variant="h6">🆕 Новые товары ({newProducts?.totalElements ?? 0})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <ProductGrid
                        products={newProducts?.content ?? []}
                        page={newPage}
                        totalPages={newProducts?.totalPages ?? 1}
                        onPageChange={(_, val) => setNewPage(val)}
                    />
                </AccordionDetails>
            </Accordion>

            <Accordion defaultExpanded sx={{ mt: 2 }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h6">📉 Пропавшие товары ({oldProducts?.totalElements ?? 0})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <ProductGrid
                        products={oldProducts?.content ?? []}
                        page={oldPage}
                        totalPages={oldProducts?.totalPages ?? 1}
                        onPageChange={(_, val) => setOldPage(val)}
                    />
                </AccordionDetails>
            </Accordion>
        </Container>
    );
}

function ProductGrid({
                         products,
                         page,
                         totalPages,
                         onPageChange,
                     }: {
    products: Product[],
    page: number,
    totalPages: number,
    onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void,
}) {
    if (products.length === 0) {
        return <Typography>Нет данных</Typography>;
    }

    return (
        <>
            {totalPages > 1 && (
                <Box display="flex" justifyContent="center" mb={3}>
                    <Pagination
                        count={totalPages}
                        page={page}
                        onChange={onPageChange}
                        color="primary"
                    />
                </Box>
            )}

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

            {/*/!* Если хочешь оставить пагинацию и внизу — оставь это *!/*/}
            {/*{totalPages > 1 && (*/}
            {/*    <Box display="flex" justifyContent="center" mt={3}>*/}
            {/*        <Pagination*/}
            {/*            count={totalPages}*/}
            {/*            page={page}*/}
            {/*            onChange={onPageChange}*/}
            {/*            color="primary"*/}
            {/*        />*/}
            {/*    </Box>*/}
            {/*)}*/}
        </>
    );
}
