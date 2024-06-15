import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import db from '../db.js';
import { auth } from '../middleware.js';

const router = express.Router();

router.post('/register', (req, res) => {
    const { username, password, email } = req.body;

    if (!username || !password || !email) {
        return res.status(400).json({ msg: 'Please enter all fields' });
    }

    db.query('SELECT username FROM users WHERE username = ?', [username], (err, result) => {
        if (err) throw err;
        if (result.length > 0) {
            return res.status(400).json({ msg: 'User already exists' });
        }

        bcrypt.hash(password, 10, (err, hash) => {
            if (err) throw err;

            db.query('INSERT INTO users (username, password, email) VALUES (?, ?, ?)', 
                [username, hash, email], (err, result) => {
                    if (err) throw err;
                    res.status(201).json({ msg: 'User registered successfully' });
                });
        });
    });
});

router.post('/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.status(400).json({ msg: 'Please enter all fields' });
    }

    db.query('SELECT * FROM users WHERE username = ?', [username], (err, result) => {
        if (err) throw err;
        if (result.length === 0) {
            return res.status(400).json({ msg: 'User not found' });
        }

        const user = result[0];

        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) throw err;
            if (!isMatch) {
                return res.status(400).json({ msg: 'Invalid credentials' });
            }

            const token = jwt.sign({ id: user.id }, 'your-jwt-secret', { expiresIn: '1h' });

            res.json({
                token,
                user: {
                    id: user.id,
                    username: user.username,
                    email: user.email
                }
            });
        });
    });
});

router.get('/user', auth, (req, res) => {
    db.query('SELECT id, username, email FROM users WHERE id = ?', [req.user.id], (err, result) => {
        if (err) throw err;
        res.json(result[0]);
    });
});

router.get('/history', auth, (req, res) => {
    const userId = req.user.id;
    
    db.query(
        'SELECT * FROM risk_assessment WHERE user_id = ?',
        [userId],
        (err, results) => {
            if (err) {
                console.error('Error fetching data:', err);
                return res.status(500).json({ error: 'Error fetching data from the database' });
            }
            res.json(results);
        }
    );
});

router.put('/update-profile', auth, (req, res) => {
    const { username, email } = req.body;
    const userId = req.user.id;

    if (!username && !email) {
        return res.status(400).json({ msg: 'Please enter a new username or email' });
    }

    const updates = {};
    if (username) updates.username = username;
    if (email) updates.email = email;

    db.query('UPDATE users SET ? WHERE id = ?', [updates, userId], (err, result) => {
        if (err) {
            console.error('Error updating profile:', err);
            return res.status(500).json({ error: 'Error updating profile' });
        }

        res.json({ msg: 'Profile updated successfully' });
    });
});

export default router;
