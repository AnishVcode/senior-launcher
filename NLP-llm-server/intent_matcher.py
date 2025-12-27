import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import pickle
import os

class IntentMatcher:
    """
    Optimized Intent Matcher with caching and faster vectorization
    Target: <10ms per query
    """
    
    def __init__(self, intent_dataset, cache_file='matcher_cache.pkl'):
        self.intent_dataset = intent_dataset
        self.cache_file = cache_file
        
        # Try to load cached model
        if os.path.exists(cache_file):
            print(f"📦 Loading cached matcher from {cache_file}...")
            self._load_cache()
            print(f"✅ Cached matcher loaded!")
        else:
            print(f"🔧 Building new matcher...")
            self._build_matcher()
            self._save_cache()
            print(f"✅ Matcher built and cached!")
    
    def _build_matcher(self):
        """Build vectorizer and compute embeddings"""
        # Flatten all examples with their intents
        self.examples = []
        self.intent_labels = []
        
        for intent, examples in self.intent_dataset.items():
            for example in examples:
                self.examples.append(example)
                self.intent_labels.append(intent)
        
        # Create TF-IDF vectorizer with optimized parameters
        self.vectorizer = TfidfVectorizer(
            max_features=500,  # Reduced from default (faster)
            ngram_range=(1, 2),  # Unigrams and bigrams
            lowercase=True,
            strip_accents='unicode',
            stop_words=None,  # Keep all words for better matching
            min_df=1,
            max_df=0.95
        )
        
        # Fit and transform all examples at once
        self.example_vectors = self.vectorizer.fit_transform(self.examples)
        
        print(f"   Examples: {len(self.examples)}")
        print(f"   Features: {len(self.vectorizer.get_feature_names_out())}")
    
    def _save_cache(self):
        """Save vectorizer and embeddings to disk"""
        cache_data = {
            'vectorizer': self.vectorizer,
            'example_vectors': self.example_vectors,
            'examples': self.examples,
            'intent_labels': self.intent_labels
        }
        with open(self.cache_file, 'wb') as f:
            pickle.dump(cache_data, f)
        print(f"💾 Matcher cached to {self.cache_file}")
    
    def _load_cache(self):
        """Load vectorizer and embeddings from disk"""
        with open(self.cache_file, 'rb') as f:
            cache_data = pickle.load(f)
        
        self.vectorizer = cache_data['vectorizer']
        self.example_vectors = cache_data['example_vectors']
        self.examples = cache_data['examples']
        self.intent_labels = cache_data['intent_labels']
    
    def get_top_candidates(self, query, top_k=5):
        """
        Find top K intent candidates for the query
        Target: <10ms
        """
        # Vectorize query
        query_vector = self.vectorizer.transform([query.lower()])
        
        # Compute similarities (fast with sparse matrices)
        similarities = cosine_similarity(query_vector, self.example_vectors)[0]
        
        # Get top K indices
        top_indices = np.argsort(similarities)[-top_k:][::-1]
        
        # Build results with intent aggregation
        intent_scores = {}
        intent_examples = {}
        
        for idx in top_indices:
            intent = self.intent_labels[idx]
            score = float(similarities[idx])
            example = self.examples[idx]
            
            # Keep highest score for each intent
            if intent not in intent_scores or score > intent_scores[intent]:
                intent_scores[intent] = score
                intent_examples[intent] = example
        
        # Sort by score
        results = [
            {
                'intent': intent,
                'score': score,
                'matched_example': intent_examples[intent]
            }
            for intent, score in sorted(intent_scores.items(), key=lambda x: x[1], reverse=True)
        ]
        
        return results[:top_k]