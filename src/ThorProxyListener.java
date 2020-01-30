public interface ThorProxyListener {
  public void accepted( ThorProxy tp );
  public void connected( ThorProxy tp );
  public void disconnected( ThorProxy tp );
  public void outOfBandPacket( ThorProxy tp, short source, byte [] data );
}
