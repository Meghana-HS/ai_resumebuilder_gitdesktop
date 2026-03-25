import React, { useState } from 'react';
import { FileText, Upload, Loader2, CheckCircle, AlertCircle, Target, Briefcase } from 'lucide-react';
import axiosInstance from '../api/axios';

const ATSChecker = () => {
  const [resumeText, setResumeText] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('text');

  const analyzeResume = async () => {
    if (!resumeText.trim()) {
      setError('Please enter your resume text or upload a file');
      return;
    }
    
    if (!jobDescription.trim()) {
      setError('Please enter a job description for ATS evaluation');
      return;
    }

    setLoading(true);
    setError('');
    setAnalysis(null);

    try {
      const response = await axiosInstance.post('/api/ats/analyze', {
        resumeText: resumeText,
        jobDescription: jobDescription.trim()
      });

      setAnalysis(response.data);
    } catch (err) {
      setError(err.response?.data || 'Failed to analyze resume. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (file.type !== 'text/plain' && !file.name.endsWith('.txt')) {
      setError('Please upload a text file (.txt)');
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      setResumeText(e.target.result);
      setError('');
    };
    reader.onerror = () => {
      setError('Failed to read file');
    };
    reader.readAsText(file);
  };

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 65) return 'text-yellow-600';
    if (score >= 50) return 'text-orange-600';
    return 'text-red-600';
  };

  const getScoreBgColor = (score) => {
    if (score >= 80) return 'bg-green-100';
    if (score >= 65) return 'bg-yellow-100';
    if (score >= 50) return 'bg-orange-100';
    return 'bg-red-100';
  };

  const formatAnalysisDisplay = (analysisText) => {
    if (!analysisText) return null;

    const sections = analysisText.split('\n\n');
    return sections.map((section, index) => {
      if (section.trim() === '') return null;
      
      const lines = section.split('\n');
      const title = lines[0];
      const content = lines.slice(1);

      return (
        <div key={index} className="mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">{title}</h3>
          <div className="text-gray-700 whitespace-pre-line">
            {content.join('\n')}
          </div>
        </div>
      );
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center mb-4">
            <Target className="w-8 h-8 text-blue-600 mr-3" />
            <h1 className="text-3xl font-bold text-gray-900">ATS Resume Checker</h1>
          </div>
          <p className="text-gray-600 max-w-2xl mx-auto">
            Compare your resume against specific job descriptions to optimize your ATS compatibility and improve your chances of getting hired.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Input Section */}
          <div className="lg:col-span-2 space-y-6">
            {/* Resume Input */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                <FileText className="w-5 h-5 mr-2 text-blue-600" />
                Resume Input
              </h2>
              
              {/* Tabs */}
              <div className="flex space-x-4 mb-4">
                <button
                  onClick={() => setActiveTab('text')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    activeTab === 'text'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  Text Input
                </button>
                <button
                  onClick={() => setActiveTab('upload')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    activeTab === 'upload'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  Upload File
                </button>
              </div>

              {/* Text Input */}
              {activeTab === 'text' && (
                <div>
                  <textarea
                    value={resumeText}
                    onChange={(e) => setResumeText(e.target.value)}
                    placeholder="Paste your resume text here..."
                    className="w-full h-48 p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  />
                  <div className="mt-2 text-sm text-gray-500">
                    {resumeText.length} characters
                  </div>
                </div>
              )}

              {/* File Upload */}
              {activeTab === 'upload' && (
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                  <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                  <label className="cursor-pointer">
                    <span className="text-blue-600 font-medium hover:text-blue-700">
                      Click to upload
                    </span>
                    <span className="text-gray-500"> or drag and drop</span>
                    <input
                      type="file"
                      accept=".txt"
                      onChange={handleFileUpload}
                      className="hidden"
                    />
                  </label>
                  <p className="text-sm text-gray-500 mt-2">Text files (.txt) only</p>
                  {resumeText && (
                    <div className="mt-4 p-3 bg-green-50 rounded-lg">
                      <p className="text-sm text-green-700">File loaded successfully!</p>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Job Description Input */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                <Briefcase className="w-5 h-5 mr-2 text-red-600" />
                Job Description (Required)
              </h2>
              <textarea
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
                placeholder="Paste the job description here... (Required for ATS evaluation)"
                className="w-full h-32 p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent resize-none"
              />
              <div className="mt-2 text-sm text-gray-500">
                {jobDescription.length} characters • Required - System needs both resume and job description
              </div>
            </div>

            {/* Error */}
            {error && (
              <div className="p-3 bg-red-50 rounded-lg flex items-center">
                <AlertCircle className="w-5 h-5 text-red-600 mr-2" />
                <p className="text-red-700 text-sm">{error}</p>
              </div>
            )}

            {/* Analyze Button */}
            <button
              onClick={analyzeResume}
              disabled={loading || !resumeText.trim() || !jobDescription.trim()}
              className={`w-full py-3 rounded-lg font-medium transition-colors flex items-center justify-center ${
                loading || !resumeText.trim() || !jobDescription.trim()
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-blue-600 text-white hover:bg-blue-700'
              }`}
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                  Analyzing Resume vs Job Description...
                </>
              ) : (
                <>
                  <Target className="w-5 h-5 mr-2" />
                  Analyze Resume vs Job Description
                </>
              )}
            </button>
          </div>

          {/* Results Section */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sticky top-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Analysis Results</h2>
              
              {analysis ? (
                <div className="space-y-6">
                  {/* Score Display */}
                  <div className={`p-6 rounded-xl ${getScoreBgColor(analysis.atsScore)}`}>
                    <div className="text-center">
                      <div className={`text-4xl font-bold ${getScoreColor(analysis.atsScore)}`}>
                        {analysis.atsScore}/100
                      </div>
                      <div className="text-gray-600 mt-2">ATS Compatibility Score</div>
                    </div>
                  </div>

                  {/* Quick Summary */}
                  <div className="border-t pt-4">
                    <h4 className="font-medium text-gray-900 mb-2">Quick Summary</h4>
                    <p className="text-sm text-gray-600">{analysis.overallEvaluation}</p>
                  </div>

                  {/* Role Detected */}
                  <div className="border-t pt-4">
                    <h4 className="font-medium text-gray-900 mb-2">Detected Role</h4>
                    <p className="text-sm text-blue-600 font-medium">{analysis.detectedRole}</p>
                  </div>

                  {/* View Full Analysis Button */}
                    <div className="border-t pt-4">
                      <button
                        onClick={() => {
                          const fullAnalysis = document.getElementById('full-analysis');
                          fullAnalysis.scrollIntoView({ behavior: 'smooth' });
                        }}
                        className="w-full py-2 px-4 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors text-sm font-medium"
                      >
                        View Full Analysis
                      </button>
                    </div>
                </div>
              ) : (
                <div className="text-center py-12">
                  <Target className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                  <p className="text-gray-500 text-sm">
                    {loading ? 'Analyzing your resume against job description...' : 'Enter both resume and job description to see results'}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Full Analysis Section */}
        {analysis && (
          <div id="full-analysis" className="mt-12 bg-white rounded-xl shadow-sm border border-gray-200 p-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Detailed Analysis Report</h2>
            {formatAnalysisDisplay(analysis)}
          </div>
        )}

        {/* Tips Section */}
        <div className="mt-8 bg-blue-50 rounded-xl p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">💡 ATS Optimization Tips</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="flex items-start">
              <CheckCircle className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
              <div>
                <p className="font-medium text-blue-900">Match Job Description</p>
                <p className="text-sm text-blue-700">Use keywords from the specific job posting</p>
              </div>
            </div>
            <div className="flex items-start">
              <CheckCircle className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
              <div>
                <p className="font-medium text-blue-900">Use Standard Formatting</p>
                <p className="text-sm text-blue-700">Avoid fancy fonts, tables, or graphics</p>
              </div>
            </div>
            <div className="flex items-start">
              <CheckCircle className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
              <div>
                <p className="font-medium text-blue-900">Quantify Achievements</p>
                <p className="text-sm text-blue-700">Use numbers and metrics to show impact</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ATSChecker;
