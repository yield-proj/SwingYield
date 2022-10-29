/*
 * Copyright [2022] [Xebisco]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebisco.swingyield;

import com.xebisco.swingyield.exceptions.NotCapableTextureException;
import com.xebisco.yield.Color;
import com.xebisco.yield.*;
import com.xebisco.yield.config.WindowConfiguration;
import com.xebisco.yield.exceptions.AudioClipException;
import com.xebisco.yield.exceptions.CannotLoadException;
import com.xebisco.yield.render.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Xebisco
 * @since 4-1.2
 */
public class SwingYield extends JPanel implements RenderMaster, KeyListener, MouseListener, ExceptionThrower, WindowPrint {

    private Graphics g;

    private JFrame frame;

    public boolean accelerateTextures;

    private Set<Renderable> renderables;
    private double savedRotation;

    private final SwingYield swingYield;

    private YldTask threadTask;

    private Vector2 savedRotationPoint = new Vector2();

    private Image image;
    private final Set<Integer> pressing = new HashSet<>();
    private HashMap<Integer, Clip> clips = new HashMap<>();
    private HashMap<String, Font> fonts = new HashMap<>();

    private Color backgroundColor;
    private float fps;

    private static final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private long last, actual;
    private AffineTransform affinetransform = new AffineTransform();
    private FontRenderContext frc = new FontRenderContext(affinetransform,true,true);

    public SwingYield() {
        swingYield = this;
    }

    public static java.awt.Color toAWTColor(Color color) {
        return new java.awt.Color(color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static Color toYieldColor(java.awt.Color color) {
        return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public void rotate(Vector2 point, Float angle) {
        ((Graphics2D) g).rotate(Math.toRadians(-angle), point.x, point.y);
    }

    @Override
    public void start(Set<Renderable> renderables) {
        this.renderables = renderables;
        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("sun.java2d.d3d", "False");
        System.setProperty("sun.java2d.pmoffscreen", "False");
        System.setProperty("sun.java2d.noddraw", "True");
    }

    @Override
    public Texture print(Vector2 pos, Vector2 size) {
        BufferedImage image1 = new BufferedImage((int) size.x, (int) size.y, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image1.getGraphics();
        g.drawImage(image, (int) -(pos.x - size.x / 2f), (int) -(pos.y - size.x / 2f), null);
        g.dispose();
        Texture t = new Texture("");
        loadTexture(t, image1, 0, 0);
        return t;
    }

    /*public class SwingGraphics implements SampleGraphics {
        @Override
        public void setRotation(Vector2 point, float angle) {
            ((Graphics2D) g).setTransform(defTransform);
            savedRotation = Math.toRadians(-angle);
            savedRotationPoint = point;
            ((Graphics2D) g).rotate(savedRotation, point.x, point.y);
        }

        @Override
        public void drawLine(Vector2 point1, Vector2 point2, Color color) {
            g.setColor(toAWTColor(color));
            g.drawLine((int) point1.x, (int) point1.y, (int) point2.x, (int) point2.y);
        }

        @Override
        public void drawRect(Vector2 pos, Vector2 size, Color color, boolean filled) {
            g.setColor(toAWTColor(color));
            if (pos.x - size.x < view.getWidth() &&
                    pos.x + size.x > 0 &&
                    pos.y - size.y < view.getHeight() &&
                    pos.y + size.y > 0)
                if (filled) {
                    g.fillRect((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y);
                } else {
                    g.drawRect((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y);
                }
        }

        @Override
        public void drawRoundRect(Vector2 pos, Vector2 size, Color color, boolean filled, int arcWidth, int arcHeight) {
            g.setColor(toAWTColor(color));
            if (pos.x - size.x < view.getWidth() &&
                    pos.x + size.x > 0 &&
                    pos.y - size.y < view.getHeight() &&
                    pos.y + size.y > 0)
                if (filled) {
                    g.fillRoundRect((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y, arcWidth, arcHeight);
                } else {
                    g.drawRoundRect((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y, arcWidth, arcHeight);
                }
        }

        @Override
        public void drawOval(Vector2 pos, Vector2 size, Color color, boolean filled) {
            g.setColor(toAWTColor(color));
            if (pos.x - size.x < view.getWidth() &&
                    pos.x + size.x > 0 &&
                    pos.y - size.y < view.getHeight() &&
                    pos.y + size.y > 0)
                if (filled) {
                    g.fillOval((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y);
                } else {
                    g.drawOval((int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y);
                }
        }

        @Override
        public void drawArc(Vector2 pos, Vector2 size, Color color, boolean filled, int startAngle, int arcAngle) {
            g.setColor(toAWTColor(color));
            if (pos.x - size.x < view.getWidth() &&
                    pos.x + size.x > 0 &&
                    pos.y - size.y < view.getHeight() &&
                    pos.y + size.y > 0)
                if (filled) {
                    g.fillArc((int) pos.x, (int) pos.y, (int) size.x, (int) size.y, startAngle, arcAngle);
                } else {
                    g.drawArc((int) pos.x, (int) pos.y, (int) size.x, (int) size.y, startAngle, arcAngle);
                }
        }

        @Override
        public void drawString(String str, Color color, Vector2 pos, Vector2 scale, String fontName) {
            g.setColor(toAWTColor(color));
            setFont(fontName);
            AffineTransform af = new AffineTransform();
            af.concatenate(AffineTransform.getScaleInstance(scale.x, scale.y));
            ((Graphics2D) g).setTransform(af);
            ((Graphics2D) g).rotate(savedRotation, savedRotationPoint.x, savedRotationPoint.y);
            af = null;

            g.drawString(str, (int) (pos.x - getStringWidth(str) / 2), (int) (pos.y + (getStringHeight(str) / 4)));
            ((Graphics2D) g).setTransform(defTransform);
        }

        @Override
        public void drawTexture(Texture texture, Vector2 pos, Vector2 size) {
            Image image = images.get(texture.getTextureID());
            if (pos.x - size.x < view.getWidth() &&
                    pos.x + size.x > 0 &&
                    pos.y - size.y < view.getHeight() &&
                    pos.y + size.y > 0)
                g.drawImage(image, (int) (pos.x - size.x / 2), (int) (pos.y - size.y / 2), (int) size.x, (int) size.y, null);
        }

        @Override
        public void setFilter(Filter filter) {
            if (filter == Filter.LINEAR) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else if (filter == Filter.NEAREST) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }

        @Override
        public void setFont(String fontName) {
            g.setFont(fonts.get(fontName));
        }

        @Override
        public float getStringWidth(String str) {
            return g.getFontMetrics().stringWidth(str);
        }

        @Override
        public float getStringWidth(String str, String font) {
            return g.getFontMetrics(fonts.get(font)).stringWidth(str);
        }

        @Override
        public float getStringHeight(String str) {
            return (float) g.getFontMetrics().getStringBounds(str, g).getHeight();
        }

        @Override
        public float getStringHeight(String str, String font) {
            return (float) g.getFontMetrics(fonts.get(font)).getStringBounds(str, g).getHeight();
        }

        @Override
        public void custom(String instruction, Object... args) {
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < types.length; i++) {
                types[i] = args[i].getClass();
            }
            try {
                SwingYield.class.getDeclaredMethod(instruction, types).invoke(swingYield, args);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    @Override
    public Texture duplicate(Texture texture) {
        Texture t = new Texture(texture.getCachedPath());
        Image img = (Image) texture.getSpecificImage();
        loadTexture(t, img.getScaledInstance(img.getWidth(null), img.getHeight(null), Image.SCALE_DEFAULT), 0, 0);
        return t;
    }

    @Override
    public Texture overlayTexture(Texture tex1, Texture tex2, Vector2 pos1, Vector2 pos2) {
        Image i1 = (Image) tex1.getSpecificImage(), i2 = (Image) tex2.getSpecificImage();
        Texture tex = new Texture((String) null);
        loadTexture(tex, i1, (int) pos1.x, (int) pos1.y);
        Graphics g = ((Image) tex.getSpecificImage()).getGraphics();
        g.drawImage(i2, (int) pos2.x, (int) pos2.y, null);
        g.dispose();
        return tex;
    }

    public void drawTexture(Texture texture, Vector2 pos, Vector2 size) {
        Image image = (Image) texture.getSpecificImage();
        g.drawImage(image, (int) (pos.x), (int) (pos.y), (int) size.x, (int) size.y, null);
    }

    public void changeWindowIcon(Texture icon) {
        Image i = (Image) icon.getSpecificImage();
        frame.setIconImage(i);
    }

    @Override
    public void loadAudioPlayer(AudioPlayer player) {
        try {
            clips.put(player.getPlayerID(), AudioSystem.getClip());
        } catch (LineUnavailableException e) {
            Yld.throwException(e);
        }
    }

    @Override
    public void pausePlayer(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null)
            if (clip.isOpen()) {
                clip.stop();
            }
    }

    @Override
    public void resumePlayer(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip.isOpen()) {
            clip.start();
        }
    }

    @Override
    public void setMicrosecondPosition(AudioPlayer audioPlayer, long pos) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null)
            if (clip.isOpen()) {
                clip.setMicrosecondPosition(pos);
            }
    }

    @Override
    public long getMicrosecondPosition(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null && clip.isOpen()) {
            return clip.getMicrosecondPosition();
        } else {
            return 0;
        }
    }

    @Override
    public long getMicrosecondLength(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null && clip.isOpen()) {
            return clip.getMicrosecondLength();
        } else {
            return 0;
        }
    }

    @Override
    public float getVolume(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null && clip.isOpen()) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            return (float) Math.pow(10f, gainControl.getValue() / 20f);
        } else return 0;
    }

    @Override
    public void setVolume(AudioPlayer audioPlayer, float value) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null && clip.isOpen()) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(value));
        }
    }

    @Override
    public void unloadPlayer(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip != null && clip.isOpen()) {
            clip.close();
            clip.flush();
            clips.remove(audioPlayer.getPlayerID());
        }
    }

    @Override
    public void unloadAllPlayers() {
        for (Clip clip : clips.values()) {
            Yld.debug(() -> Yld.getDebugLogger().log("Flushed: " + clip));
            if (clip != null) {
                if (clip.isOpen()) {
                    clip.close();
                    clip.flush();
                }
            }
        }
        while (!clips.isEmpty())
            clips.clear();
    }

    @Override
    public void setLoop(AudioPlayer audioPlayer, boolean loop) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip.isOpen()) {
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.loop(0);
            }
        }
    }

    @Override
    public void setLoop(AudioPlayer audioPlayer, int count) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip.isOpen()) {
            clip.loop(count);
        }
    }

    @Override
    public boolean isPlayerRunning(AudioPlayer audioPlayer) {
        Clip clip = clips.get(audioPlayer.getPlayerID());
        if (clip.isOpen()) {
            return clip.isRunning();
        } else {
            return false;
        }
    }

    @Override
    public void loadAudioClip(AudioClip audioClip, AudioPlayer audioPlayer) {
        try {
            Clip clip = clips.get(audioPlayer.getPlayerID());
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(audioClip.getInputStream());
            clip.close();
            clip.open(inputStream);
            if (audioClip.isFlushAfterLoad())
                audioClip.flush();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            Yld.throwException(e);
        } catch (NullPointerException e) {
            Yld.throwException(new AudioClipException("Cannot find audio file: '" + audioClip.getCachedPath() + "'"));
        }
    }

    @Override
    public SampleWindow initWindow(WindowConfiguration configuration) {
        SampleWindow sampleWindow = sampleWindow(configuration);
        addPanel();
        showWindow(configuration);
        /*if (Yld.debug) {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/
        return sampleWindow;
    }

    public void addPanel() {
        frame.add(this);
    }

    public SampleWindow sampleWindow(WindowConfiguration configuration) {
        Toolkit.getDefaultToolkit().setDynamicLayout(false);
        frame = new JFrame();
        frame.setSize(configuration.width, configuration.height);
        frame.setResizable(configuration.resizable);
        frame.setAlwaysOnTop(configuration.alwaysOnTop);
        frame.setTitle(configuration.title);
        frame.setUndecorated(configuration.undecorated);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return new SampleWindow() {
            @Override
            public int getWidth() {
                return frame.getWidth();
            }

            @Override
            public int getHeight() {
                return frame.getHeight();
            }
        };
    }

    public void showWindow(WindowConfiguration configuration) {
        if (configuration.fullscreen) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
        }
        frame.setVisible(true);
        if (!configuration.fullscreen) {
            frame.setSize(configuration.width + frame.getInsets().right + frame.getInsets().left, configuration.height + frame.getInsets().top + frame.getInsets().bottom);
        }
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        if (configuration.hideMouse) {
            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(Objects.requireNonNull(Yld.class.getResource("assets/none.png"))).getImage(), new Point(), "none");
            frame.setCursor(cursor);
        }
        changeWindowIcon(configuration.icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        actual = System.currentTimeMillis();
        fps = 1000 / (float) (actual - last);
        g.setColor(java.awt.Color.BLACK);
        int w = frame.getWidth() - frame.getInsets().right - frame.getInsets().left,
                h = frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom;
        g.fillRect(0, 0, w, h);
        if (image != null) {
            Graphics2D g2 = (Graphics2D) image.getGraphics();
            g2.setColor(toAWTColor(backgroundColor));
            g2.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
            for (Renderable renderable : renderables) {
                if (renderable.getType() != RenderableType.IMAGE) {
                    if (renderable.getSpecificColor() == null)
                        renderable.setSpecificColor(toAWTColor(renderable.getColor()));
                    g2.setColor((java.awt.Color) renderable.getSpecificColor());
                }
                g2.setTransform((AffineTransform) affinetransform.clone());
                g2.rotate(Math.toRadians(-renderable.getRotation()), renderable.getX(), renderable.getY());
                switch (renderable.getType()) {
                    case LINE:
                        g2.setStroke(new BasicStroke(renderable.getThickness()));
                        g2.drawLine(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getX() + renderable.getWidth() / 2, renderable.getY() + renderable.getHeight() / 2);
                        break;
                    case RECTANGLE:
                        if (renderable.isFilled())
                            g2.fillRect(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight());
                        else {
                            g2.setStroke(new BasicStroke(renderable.getThickness()));
                            g2.drawRect(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight());
                        }
                        break;
                    case OVAL:
                        if (renderable.isFilled())
                            g2.fillOval(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight());
                        else {
                            g2.setStroke(new BasicStroke(renderable.getThickness()));
                            g2.drawOval(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight());
                        }
                        break;
                    case ROUNDED_RECTANGLE:
                        if (renderable.isFilled())
                            g2.fillRoundRect(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight(), renderable.getArcWidth(), renderable.getArcHeight());
                        else {
                            g2.setStroke(new BasicStroke(renderable.getThickness()));
                            g2.drawRoundRect(renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight(), renderable.getArcWidth(), renderable.getArcHeight());
                        }
                        break;
                    case IMAGE:
                        g2.drawImage((Image) renderable.getSpecific(), renderable.getX() - renderable.getWidth() / 2, renderable.getY() - renderable.getHeight() / 2, renderable.getWidth(), renderable.getHeight(), null);
                        break;
                    case TEXT:
                        String[] ss = renderable.getSpecific().toString().split("\1");
                        g2.setFont(fonts.get(ss[1]));
                        g2.drawString(ss[0], renderable.getX() - getStringWidth(ss[0], ss[1]) / 2f, renderable.getY() + getStringHeight(ss[0], ss[1]) / 4f);
                        break;
                }
            }
            g2.dispose();
            //g2.rotate(Math.toRadians(view.getTransform().rotation) * -1, w / 2f, h / 2f);
            //float vsx = view.getTransform().scale.x, vpx = view.getPosition().x, vsy = view.getTransform().scale.y, vpy = view.getPosition().y;
            float vsx = 1, vpx = 0, vsy = 1, vpy = 0;
            try {
                g.drawImage(image, (int) (w / 2 - w * vsx / 2 + vpx), (int) (h / 2 - h * vsy / 2 + vpy), (int) (w * vsx), (int) (h * vsy), null);
            } catch (NullPointerException ignore) {
            }
        }
         /*if(!started) {
             g.setColor(java.awt.Color.WHITE);
             g.fillRect(0, 0, w, h);
             g.drawImage(yieldImage, getWidth() / 2 - yieldImage.getWidth() / 2, getHeight() / 2 - yieldImage.getHeight() / 2, null);
         }*/
        g.dispose();
        Toolkit.getDefaultToolkit().sync();
        last = System.currentTimeMillis();
        threadTask.execute();
    }
    /*public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            try {
                createBufferStrategy(2, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBufferCapabilities());
                return;
            } catch (AWTException e) {
                Yld.throwException(e);
            }
        }
        assert bs != null;
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        actual = System.currentTimeMillis();
        fps = 1000 / (float) (actual - last);
        g.setColor(java.awt.Color.BLACK);
        int w = frame.getWidth() - frame.getInsets().right - frame.getInsets().left,
                h = frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom;
        g.fillRect(0, 0, w, h);
        this.g = g;
        if (defTransform == null)
            defTransform = ((Graphics2D) this.g).getTransform();
        this.g.setColor(toAWTColor(view.getBgColor()));
        this.g.fillRect(0, 0, view.getWidth(), view.getHeight());
        if (view != null) {
            started = true;
            //g.rotate(Math.toRadians(view.getTransform().rotation) * -1, w / 2f, h / 2f);
            float vsx = view.getTransform().scale.x, vpx = view.getPosition().x, vsy = view.getTransform().scale.y, vpy = view.getPosition().y;
            try {
                g.drawImage(image, (int) (w / 2 - w * vsx / 2 + vpx), (int) (h / 2 - h * vsy / 2 + vpy), (int) (w * vsx), (int) (h * vsy), null);
            } catch (NullPointerException ignore) {
            }
        }
       /* if(!started) {
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.drawImage(yieldImage, getWidth() / 2 - yieldImage.getWidth() / 2, getHeight() / 2 - yieldImage.getHeight() / 2, null);
        }*//*
        g.dispose();
        bs.show();
        Toolkit.getDefaultToolkit().sync();
        last = System.currentTimeMillis();
    }*/

    @Override
    public void frameEnd(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        Yld.debug(() -> {
            if (!frame.getTitle().endsWith(" (DEBUG)"))
                frame.setTitle(frame.getTitle() + " (DEBUG)");
        });
        Yld.release(() -> {
            if (frame.getTitle().endsWith(" (DEBUG)"))
                frame.setTitle(frame.getTitle().substring(0, " (DEBUG)".length()));
        });
        repaint();
        if (g != null)
            g.dispose();
    }

    @Override
    public void onResize(int width, int height) {
        if (image != null)
            image.flush();
        image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        image.setAccelerationPriority(1);
        Graphics gi = image.getGraphics();
        gi.setColor(java.awt.Color.BLACK);
        gi.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        gi.dispose();
        //image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public float fpsCount() {
        return fps;
    }

    @Override
    public void loadTexture(Texture texture) {
        try {
            InputStream in = texture.getInputStream();
            if (in == null)
                Yld.throwException(new CannotLoadException("Could not find the texture: '" + texture.getCachedPath() + "'"));
            else
                loadTexture(texture, ImageIO.read(in), 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTexture(Texture texture, Image img, int ix, int iy) {
        Image image, imageX, imageY, imageXY;
        int width = img.getWidth(null), height = img.getHeight(null);
        if (accelerateTextures) {
            image = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
            imageX = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
            imageY = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
            imageXY = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
        } else {
            image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            imageX = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            imageY = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            imageXY = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        }
        image.setAccelerationPriority(1);
        imageX.setAccelerationPriority(1);
        imageY.setAccelerationPriority(1);
        imageXY.setAccelerationPriority(1);
        Graphics g = image.getGraphics();
        g.drawImage(img, ix, iy, this);
        g.dispose();
        img.flush();
        Graphics xg = imageX.getGraphics();
        xg.drawImage(image, width, 0, -width, height, this);
        xg.dispose();
        Graphics yg = imageY.getGraphics();
        yg.drawImage(image, 0, height, width, -height, this);
        yg.dispose();
        Graphics xyg = imageXY.getGraphics();
        xyg.drawImage(image, width, height, -width, -height, this);
        xyg.dispose();
        Texture invX = new Texture(""), invY = new Texture(""), invXY = new Texture("");
        invX.setWidth(imageX.getWidth(null));
        invX.setHeight(imageX.getHeight(null));
        invX.setSpecificImage(imageX);
        invY.setWidth(imageY.getWidth(null));
        invY.setHeight(imageY.getHeight(null));
        invY.setSpecificImage(imageY);
        invXY.setWidth(imageXY.getWidth(null));
        invXY.setHeight(imageXY.getHeight(null));
        invXY.setSpecificImage(imageXY);
        invXY.setVisualUtils(this);
        invY.setVisualUtils(this);
        invXY.setVisualUtils(this);
        texture.setInvertedX(invX);
        texture.setInvertedY(invY);
        texture.setInvertedXY(invXY);
        texture.setWidth(width);
        texture.setHeight(height);
        texture.setSpecificImage(image);
        texture.setVisualUtils(this);
        if (texture.isFlushAfterLoad())
            texture.flush();
    }

    @Override
    public void setPixel(Texture texture, Color color, Vector2 position) {
        Image i = (Image) texture.getSpecificImage();
        if (i instanceof BufferedImage) {
            BufferedImage img = (BufferedImage) i;
            try {
                img.setRGB((int) position.x, (int) position.y, toAWTColor(color).getRGB());
            } catch (ArrayIndexOutOfBoundsException ignore) {
            }
        } else {
            throw new NotCapableTextureException();
        }
    }

    @Override
    public void unloadTexture(Texture texture) {
        ((Image) texture.getSpecificImage()).flush();
    }

    @Override
    public void unloadAllTextures() {

        //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        /*for (Image img : images.values()) {
            if (img != null)
                img.flush();
        }*/
    }

    @Override
    public void clearTexture(Texture texture) {
        if (accelerateTextures) {
            texture.setSpecificImage(gc.createCompatibleImage(texture.getWidth(), texture.getHeight(), Transparency.TRANSLUCENT));
        } else {
            texture.setSpecificImage(new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB));
        }
    }

    @Override
    public void loadFont(String saveName, String fontName, float fontSize, int fontStyle) {
        fonts.put(saveName, new Font(fontName, fontStyle, (int) fontSize));
    }

    @Override
    public void loadFont(String fontName, float size, float sizeToLoad, int fontFormat, RelativeFile relativeFile) {
        try {
            fonts.put(fontName, Font.createFont(fontFormat, relativeFile.getInputStream()).deriveFont(sizeToLoad).deriveFont(size));
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Texture cutTexture(Texture texture, int x, int y, int width, int height) {
        Texture tex;
        Image img = (Image) texture.getSpecificImage();
        if (img instanceof BufferedImage) {
            img = ((BufferedImage) img).getSubimage(x, y, width, height);
        } else {
            img = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
            Graphics g = img.getGraphics();
            g.drawImage((Image) texture.getSpecificImage(), -x, -y, null);
            g.dispose();
        }
        tex = new Texture((String) null);
        loadTexture(tex, img, 0, 0);
        return tex;
    }

    @Override
    public Texture scaleTexture(Texture texture, int width, int height) {
        Texture tex = new Texture((String) null);
        loadTexture(tex, ((Image) texture.getSpecificImage()).getScaledInstance(width, height, Image.SCALE_DEFAULT), 0, 0);
        return tex;
    }

    @Override
    public Color[][] getTextureColors(Texture texture) {
        Image image1 = (Image) texture.getSpecificImage();
        if (image1 instanceof BufferedImage) {
            BufferedImage image2 = (BufferedImage) image1;
            Color[][] pixels = new Color[image2.getWidth()][image2.getHeight()];
            for (int x = 0; x < pixels.length; x++) {
                for (int y = 0; y < pixels[0].length; y++) {
                    int p = image2.getRGB(x, y);
                    pixels[x][y] = (new Color(((p >> 16) & 0xff) / 255f, ((p >> 8) & 0xff) / 255f, (p & 0xff) / 255f, ((p >> 24) & 0xff) / 255f));
                }
            }
            image1.flush();
            return pixels;
        } else {
            throw new NotCapableTextureException();
        }
    }

    @Override
    public void setTextureColors(Texture texture, Color[][] colors) {
        Image image1 = (Image) texture.getSpecificImage();
        if (image1 instanceof BufferedImage) {
            BufferedImage img = (BufferedImage) image1;
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[0].length; y++) {
                    img.setRGB(x, y, toAWTColor(colors[x][y]).getRGB());
                }
            }
        } else {
            throw new NotCapableTextureException();
        }
    }

    public SwingYield getSwingYield() {
        return swingYield;
    }

    @Override
    public void unloadFont(String fontName) {
        fonts.remove(fontName);
    }

    @Override
    public Set<Integer> pressing() {
        return pressing;
    }

    /* @Override
     public int mouseX() {
         return Yld.clamp(8 + (int) (((float) MouseInfo.getPointerInfo().getLocation().x - frame.getInsets().left - frame.getInsets().right - frame.getX()) / (float) getWidth() * (float) view.getWidth()), 0, view.getWidth());
     }

     @Override
     public int mouseY() {
         return Yld.clamp(8 + (int) (((float) MouseInfo.getPointerInfo().getLocation().y - frame.getInsets().top - frame.getInsets().bottom - frame.getY()) / (float) getHeight() * (float) view.getHeight()), 0, view.getHeight());
     }*/
    @Override
    public int mouseX() {
        return 0;
    }

    @Override
    public int mouseY() {
        return 0;
    }

    @Override
    public void setThreadTask(YldTask threadTask) {
        this.threadTask = threadTask;
    }

    @Override
    public float getStringWidth(String s, String s1) {
        return (float) fonts.get(s1).getStringBounds(s, frc).getWidth();
    }

    @Override
    public float getStringHeight(String s, String s1) {
        return (float) fonts.get(s1).getStringBounds(s, frc).getHeight();
    }

    @Override
    public void throwException(Exception e) {
        if (JOptionPane.showOptionDialog(null,
                e.getMessage() + "\nContinue execution? This can make the running game unstable.",
                e.getClass().getSimpleName(), JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new String[]{"Yes", "No"},
                "No") == 1) {
            System.exit(1);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressing.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressing.remove(e.getKeyCode());
    }

    @Override
    public boolean canStart() {
        return true;
    }

    public Graphics getG() {
        return g;
    }

    public void setG(Graphics g) {
        this.g = g;
    }

    public HashMap<String, Font> getFonts() {
        return fonts;
    }

    public void setFonts(HashMap<String, Font> fonts) {
        this.fonts = fonts;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressing.add(-e.getButton() - 1);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressing.remove(-e.getButton() - 1);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public double getSavedRotation() {
        return savedRotation;
    }

    public void setSavedRotation(double savedRotation) {
        this.savedRotation = savedRotation;
    }

    public Vector2 getSavedRotationPoint() {
        return savedRotationPoint;
    }

    public void setSavedRotationPoint(Vector2 savedRotationPoint) {
        this.savedRotationPoint = savedRotationPoint;
    }

    public Set<Integer> getPressing() {
        return pressing;
    }

    public float getFps() {
        return fps;
    }

    public void setFps(float fps) {
        this.fps = fps;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public long getActual() {
        return actual;
    }

    public void setActual(long actual) {
        this.actual = actual;
    }

    public HashMap<Integer, Clip> getClips() {
        return clips;
    }

    public void setClips(HashMap<Integer, Clip> clips) {
        this.clips = clips;
    }

    public boolean isAccelerateTextures() {
        return accelerateTextures;
    }

    public void setAccelerateTextures(boolean accelerateTextures) {
        this.accelerateTextures = accelerateTextures;
    }
}
