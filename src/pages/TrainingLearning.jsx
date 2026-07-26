import Layout from '../components/Layout';
import MyTrainingSection from '../components/dashboards/MyTrainingSection';

export default function TrainingLearning() {
  return (
    <Layout
      title="Training & Learning"
      subtitle="Enroll in courses, track your progress, and complete your learning path"
    >
      <MyTrainingSection />
    </Layout>
  );
}
