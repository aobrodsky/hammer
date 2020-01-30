public interface Task {
  enum Type { 
    NORMAL,
    START_BLOCK,
    START_PATH,
    START_POLY,
    NEXT_POINT,
    DRAG_ARTIFACT,
    DRAG_HANDLE,
    PLACE_LIBOBJ,
    START_ROBOT,
  };

  public void setTask( Type t );
  public Type getTask();
}
