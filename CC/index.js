import express from "express";
import authRoutes from './routes/authen.js';

const app = express(); 
const PORT = 3000; 

app.use(express.json());
app.use('/api/auth', authRoutes);

app.listen(PORT, (error) =>{ 
	if(!error) {
		console.log(`Selamat ngoding`);
		console.log(`Server started on port: http://localhost:${PORT}`);
	}else
		console.log("Error occurred, server can't start", error); 
	} 
); 