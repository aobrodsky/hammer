public interface EnvListener {
  public enum StateChange {
    USER_ACTION,
    POSITION,
    OBJECT,
    DELETE,
    ENVIRONMENT
  };

  public void updateAll();
  public void updateArena();
  public void stateChanged( Artifact t, StateChange typ );
  public void modelChanged( EnvModel e );
  public void objectSelected( Artifact a );
  public void objectInstantiatedFromLibrary( Artifact a );
}
