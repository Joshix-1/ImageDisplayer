
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame implements MouseListener {
    static final private int APS = 400; //how often the Drawing checks for updates
    static final private int FPS = 30; //how often everything gets displayed
    static final private int EDGE_SIZE = 20;
    static final public Color BG = Color.WHITE;
    private DrawImage image;
    private int imgOffsetX, imgOffsetY;
    private int imgDisplayW,imgDisplayH;
    private int counter = 0;
    private File imgFile;
    private ScheduledExecutorService draw_schedule = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService remove_schedule = Executors.newScheduledThreadPool(1);

    public static void main(String[] args)  {
        if(args.length <= 0) new Main(new BufferedImage(420, 420,BufferedImage.TYPE_INT_ARGB)); //CopyImage.getImageFromClipboard().ifPresent(Main::new);
        else for (String arg : args) {
            new Main(arg, args.length);
        }
    }

    private Main(Image image) {
        this("New Image", 1);
        setImage(image, "new Image");
    }

    private Main(String imageName, int window_count) {
        repaint();
        setSize(666, 420);
        setVisible(true);
        addMouseListener(this);
        setBackground(BG);
        if(window_count == 1) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addMouseWheelListener(this::mouseWheelMoved);

        setImage(imageName);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                image.setChanged();
            }
        });

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::paint, 10L, 1000/FPS, TimeUnit.MILLISECONDS);
    }


    private void paint() {
        if(image == null) return;
        setTitle(image.getTitle());
        if(isActive() && image.hasChanged()) {
            if(counter++ > 50) {
                setIconImage(image.getCopyImage());
                counter = 0;
            }

            if(image.hasDrawn()) {
                image.drawCircles();
            }

            Graphics g = getGraphics();
            g.setPaintMode();

            double ratio = (double) (image.getWidth()*1000) /((double) image.getHeight() * 1000);

            imgDisplayW = (int) ((getWidth() - (2 * EDGE_SIZE)) * 0.88);
            imgDisplayW = Math.min(imgDisplayW, (int) (ratio * ((double) (getHeight() - (2 * EDGE_SIZE)) * 0.88)));

            imgDisplayH = (int) (imgDisplayW/ratio);

            image.setEdgeSize(EDGE_SIZE * image.getWidth()/imgDisplayW);


            BufferedImage copy = image.getDisplayImage(imgDisplayW, imgDisplayH);

            imgOffsetX = getWidth() / 2 - (imgDisplayW / 2);
            imgOffsetY = getHeight() / 2 - (imgDisplayH / 2) + 5;

            g.setColor(image.getDrawingColor());

            g.fillRect(0, 0, getWidth(), imgOffsetY - EDGE_SIZE);
            g.fillRect(0, imgOffsetY - EDGE_SIZE, imgOffsetX - EDGE_SIZE, EDGE_SIZE + getHeight() - imgOffsetY);
            g.fillRect(imgOffsetX + copy.getWidth() + EDGE_SIZE, imgOffsetY - EDGE_SIZE,  getWidth() - (imgOffsetX + copy.getWidth()), EDGE_SIZE + getHeight() - imgOffsetY);
            g.fillRect(imgOffsetX - EDGE_SIZE, imgOffsetY + copy.getHeight() + EDGE_SIZE, EDGE_SIZE + getWidth() - imgOffsetX, getHeight() - (imgOffsetY + copy.getHeight()));

            g.setColor(BG);
            g.fillRect(imgOffsetX - EDGE_SIZE, imgOffsetY, EDGE_SIZE, imgDisplayH); //edge left
            g.fillRect(imgOffsetX - EDGE_SIZE, imgOffsetY - EDGE_SIZE, imgDisplayW + (EDGE_SIZE * 2), EDGE_SIZE); //edge top
            g.fillRect(imgOffsetX + imgDisplayW - 1, imgOffsetY, EDGE_SIZE, imgDisplayH); //edge right
            g.fillRect(imgOffsetX - EDGE_SIZE, imgOffsetY + imgDisplayH - 1, imgDisplayW + (EDGE_SIZE * 2), EDGE_SIZE); //edge bottom

            g.setColor(Color.BLACK);
            g.drawRect(imgOffsetX - EDGE_SIZE, imgOffsetY - EDGE_SIZE, imgDisplayW + (EDGE_SIZE * 2), imgDisplayH + (EDGE_SIZE * 2));

            g.drawImage(copy, imgOffsetX - 1, imgOffsetY - 1, null);

            g.dispose();
        } else {
            if(counter != -1) {
                counter = -1;
                setIconImage(image.getCopyImage());
                image.drawCircles();
            }
        }
    }


    private void setImage(Image image, String name) {
        this.image = new DrawImage(image, name);
        setTitle(this.image.getTitle());
    }

    private void setImage(String image) {
        getImage(image).ifPresent(image1 -> setImage(image1, image));
    }

    private Optional<Image> getImage(String image) {
        if(image == null || image.isEmpty()) return Optional.empty();
        try {
            return Optional.of(ImageIO.read(new URL(image)));
        } catch (IOException ignored) {}
        try {
            if(image.contains("?")) return Optional.of(ImageIO.read(new URL(image.substring(0, image.indexOf("?") - 1))));
        } catch(IOException ignored) {}

        imgFile = new File(image);
        if(imgFile.exists() && imgFile.isFile()) {
            return Optional.of(new ImageIcon(image).getImage());
        } else {
            imgFile = null;
            return Optional.empty();
        }
    }

    private Point pointOnNormalSizedImage(Point p) {
        int xOnWindow = (int) ((p.x - imgOffsetX) * ((double) image.getWidth() / (double) imgDisplayW));
        int yOnWindow = (int) ((p.y - imgOffsetY) * ((double) image.getHeight() / (double) imgDisplayH));
        return new Point(xOnWindow, yOnWindow);
    }
    
    private Point currentMouseLocation() {
        Point p = MouseInfo.getPointerInfo().getLocation();
        Point wLoc = getLocation();
        p.translate(-wLoc.x, -wLoc.y);

        return pointOnNormalSizedImage(p);
    }

    private boolean pointIsOnImage(Point p) {
        return p.x >= 0 && p.x < image.getWidth() && p.y >= 0 && p.y < image.getHeight();
    }

    private boolean mouseIsOnSides() {
        Point p = currentMouseLocation();
        return !image.pointIsOnEdge(p) && !pointIsOnImage(p);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && mouseIsOnSides()) {
            image.changeDrawingColor();
        } else if(e.getButton() == MouseEvent.BUTTON2) {
            BufferedImage bi = image.getCopyImage();
            new CopyImage().copyImage(bi);

            if(imgFile == null) {
                image.setImageCopied(DrawImage.COPIED);
            } else {
                String[] name  = imgFile.getName().split("\\.");
                try {
                    ImageIO.write(bi, name[name.length - 1], imgFile);
                    image.setImageCopied(DrawImage.COPIED_AND_SAVED);
                    setImage(bi, image.getName());
                } catch (IOException ex) {
                    image.setImageCopied(DrawImage.COPIED);
                    ex.printStackTrace();
                }
            }

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> image.removeImageCopied(), 6L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            if(image.pointIsOnEdge(currentMouseLocation())) {
                image.startEdgeSelection(currentMouseLocation());
                draw_schedule.scheduleAtFixedRate(() ->
                                image.moveSelectedEdge(currentMouseLocation())
                        , 1000/ APS, 1000/ APS, TimeUnit.MILLISECONDS);
            } else if(pointIsOnImage(currentMouseLocation())) draw_schedule.scheduleAtFixedRate(() ->
                image.draw(currentMouseLocation())
            , 0, 1000/ APS, TimeUnit.MILLISECONDS);
        } else if(e.getButton() == MouseEvent.BUTTON3 && pointIsOnImage(currentMouseLocation())) {
            remove_schedule.scheduleAtFixedRate(() ->
                            image.remove(currentMouseLocation())
                    , 0, 1000/ APS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        try {
            if(e.getButton() == MouseEvent.BUTTON1) {
                image.unselectEdge();
                draw_schedule.shutdown();
                draw_schedule = new ScheduledThreadPoolExecutor(1);
            } else if(e.getButton() == MouseEvent.BUTTON3) {
                remove_schedule.shutdown();
                remove_schedule = new ScheduledThreadPoolExecutor(1);
            }
            image.drawCircles();
            image.resetDrawingOperations();
        } catch(Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        image.increaseDrawSize(-e.getWheelRotation());
    }
}