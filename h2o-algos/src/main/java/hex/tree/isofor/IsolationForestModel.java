package hex.tree.isofor;

import hex.ModelCategory;
import hex.ModelMetrics;
import hex.genmodel.utils.DistributionFamily;
import hex.tree.SharedTreeModel;
import water.Key;
import water.util.SBPrintStream;


public class IsolationForestModel extends SharedTreeModel<IsolationForestModel, IsolationForestModel.IsolationForestParameters, IsolationForestModel.IsolationForestOutput> {

  public static class IsolationForestParameters extends SharedTreeModel.SharedTreeParameters {
    public String algoName() { return "IsolationForest"; }
    public String fullName() { return "Isolation Forest"; }
    public String javaName() { return IsolationForestModel.class.getName(); }
    public int _mtries;

    public IsolationForestParameters() {
      super();
      _mtries = -1;
      _sample_rate = 0.632f;
      _max_depth = 64;
      _min_rows = 1;
      _min_split_improvement = 0;
      _histogram_type = HistogramType.Random;
      _distribution = DistributionFamily.gaussian;
    }
  }

  public static class IsolationForestOutput extends SharedTreeModel.SharedTreeOutput {
    public long _max_path_length;
    public long _min_path_length;

    public IsolationForestOutput(IsolationForest b) { super(b); }

    @Override
    public ModelCategory getModelCategory() {
      return ModelCategory.AnomalyDetection;
    }
  }

  public IsolationForestModel(Key<IsolationForestModel> selfKey, IsolationForestParameters parms, IsolationForestOutput output ) { super(selfKey, parms, output); }

  @Override
  public ModelMetrics.MetricBuilder makeMetricBuilder(String[] domain) {
    return new ModelMetricsAnomaly.MetricBuilderAnomaly();
  }

  protected String[] makeScoringNames(){
    return new String[]{"predict", "mean_length"};
  }

  /** Bulk scoring API for one row.  Chunks are all compatible with the model,
   *  and expect the last Chunks are for the final distribution and prediction.
   *  Default method is to just load the data into the tmp array, then call
   *  subclass scoring logic. */
  @Override protected double[] score0(double[] data, double[] preds, double offset, int ntrees) {
    super.score0(data, preds, offset, ntrees);
    if (ntrees >= 1) preds[1] = preds[0] / ntrees;
    preds[0] = normalizePathLength(preds[0]);
    return preds;
  }

  final double normalizePathLength(double pathLength) {
    if (_output._max_path_length > _output._min_path_length) {
      return  (_output._max_path_length - pathLength) / (_output._max_path_length - _output._min_path_length);
    } else {
      return 1;
    }
  }

  @Override protected void toJavaUnifyPreds(SBPrintStream body) {
    throw new UnsupportedOperationException("Isolation Forest support only MOJOs.");
  }

  @Override
  public boolean havePojo() {
    return false;
  }

  @Override
  public IsolationForestMojoWriter getMojo() {
    return new IsolationForestMojoWriter(this);
  }

}