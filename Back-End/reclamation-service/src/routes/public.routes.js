const express = require('express');
const router = express.Router();

// Public FAQ endpoint (no authentication required)
router.get('/faqs', (req, res) => {
  const faqs = [
    {
      id: 1,
      question: "How do I submit a reclamation?",
      answer: "You can submit a reclamation by filling out the form in the 'Support' section after logging into your account."
    },
    {
      id: 2,
      question: "How long does it take to process a reclamation?",
      answer: "Most reclamations are processed within 2-3 business days. Urgent matters may be handled sooner."
    },
    {
      id: 3,
      question: "Can I track the status of my reclamation?",
      answer: "Yes, you can view the status of all your reclamations in the 'My Reclamations' section of your account."
    },
    {
      id: 4,
      question: "What information should I include in my reclamation?",
      answer: "Please include a detailed description of the issue, relevant dates/times, and any supporting documents or screenshots."
    },
    {
      id: 5,
      question: "How will I be notified about updates to my reclamation?",
      answer: "You'll receive email notifications whenever there's an update to your reclamation. You can also check the status anytime in your account."
    }
  ];
  
  res.json({
    success: true,
    count: faqs.length,
    data: faqs
  });
});

// Public contact information (no authentication required)
router.get('/contact', (req, res) => {
  res.json({
    success: true,
    data: {
      email: "support@andah.com",
      phone: "+1-800-ANDAH-HELP",
      hours: "Monday-Friday, 9am-5pm CET"
    }
  });
});

module.exports = router;
