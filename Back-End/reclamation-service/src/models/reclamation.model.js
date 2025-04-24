const mongoose = require('mongoose');

const reclamationSchema = new mongoose.Schema({
  title: {
    type: String,
    required: true,
    trim: true
  },
  description: {
    type: String,
    required: true,
    trim: true
  },
  date: {
    type: Date,
    default: Date.now
  },
  status: {
    type: String,
    enum: ['pending', 'in-progress', 'resolved', 'rejected'],
    default: 'pending'
  },
  user_id: {
    type: String,
    required: true
  },
  response: {
    text: String,
    date: Date,
    admin_id: String
  },
  category: {
    type: String,
    enum: ['payment', 'account', 'technical', 'service', 'other'],
    default: 'other'
  },
  priority: {
    type: String,
    enum: ['low', 'medium', 'high', 'urgent'],
    default: 'medium'
  }
}, {
  timestamps: true
});

// Add indexes for faster queries
reclamationSchema.index({ user_id: 1 });
reclamationSchema.index({ status: 1 });
reclamationSchema.index({ date: -1 });

const Reclamation = mongoose.model('Reclamation', reclamationSchema);

module.exports = Reclamation;
