import express from 'express';
import { auth } from '../middleware.js';

const router = express.Router();

router.get('/prevention', auth, (req, res) => {
    const preventionAdvice = `To prevent stroke, it is important to maintain a healthy lifestyle. Start by consuming a diet rich in fruits, vegetables, and whole grains. Regular exercise, aiming for at least 30 minutes of moderate activity most days of the week, is also crucial. Avoid smoking and limit alcohol consumption. Ensure your blood pressure and cholesterol levels are well-controlled. If you have diabetes, manage it with proper medication and lifestyle changes. Maintaining a healthy weight can also reduce your risk of stroke. Lastly, stay hydrated by drinking plenty of water.`;

    res.json({ advice: preventionAdvice });
});

router.get('/rehabilitation', auth, (req, res) => {
    const rehabilitationAdvice = `For rehabilitation after a stroke, it is important to engage in regular physical therapy to improve mobility and strength. Perform exercises such as walking, swimming, or stationary cycling to enhance cardiovascular health. Balance and coordination exercises are essential to reduce the risk of falls. Strength training exercises are also necessary to rebuild muscle strength. If needed, participate in speech therapy to improve communication skills. Engage in occupational therapy to regain independence in daily activities. Mental exercises, such as puzzles or memory games, can enhance cognitive function.`;

    res.json({ advice: rehabilitationAdvice });
});

export default router;
