import mysql from 'mysql2';

const db = mysql.createConnection({
    host: '34.128.99.134',
    user: 'root',
    database: 'auth_db'
});

db.connect(err => {
    if (err) throw err;
    console.log('Connected to MySQL database');
});

export default db;
