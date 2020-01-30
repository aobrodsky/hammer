public interface RobotControl {
  public void init( Env e );
  public short getType();
  public void setTarget( short id );
  public void setTargetAll();
}
