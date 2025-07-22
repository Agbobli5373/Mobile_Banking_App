import { APP_NAME } from '@/constants';

function App() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full bg-white rounded-xl shadow-soft p-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{APP_NAME}</h1>
          <p className="text-gray-600 mb-8">
            Welcome to your mobile banking application
          </p>
          <div className="space-y-4">
            <button className="btn-primary w-full">Get Started</button>
            <button className="btn-secondary w-full">Learn More</button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
