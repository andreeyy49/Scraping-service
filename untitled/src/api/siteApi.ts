import axios from "axios";

const BASE_URL = "http://localhost:8080/api/v1/sites";

export const getAllSites = async () => {
    const response = await axios.get(BASE_URL);
    return response.data;
};

export const scrapSite = async (url: string) => {
    try {
        const response = await axios.post(`${BASE_URL}/scrape`, { url });
        return response.status === 200;
    } catch (e) {
        console.error("Ошибка при скрапинге:", e);
        return false;
    }
};

export const getSiteDetails = async (id: string) => {
    const response = await axios.get(`${BASE_URL}/${id}`);
    return response.data;
};
