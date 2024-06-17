package com.tang0488.Poem;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PoemService {
    private List<Poem> poems;
    private Random random;

    public PoemService() {
        this.poems = new ArrayList<>();
        this.random = new Random();
        loadSamplePoems();
    }

    private void loadSamplePoems() {
        poems.add(new Poem("STRAY birds of summer come to my window to sing and fly away."));
        poems.add(new Poem("And yellow leaves of autumn, which have no songs, flutter and fall there with a sigh."));
        poems.add(new Poem("O TROUPE of little vagrants of the world, leave your footprints in my words."));
        poems.add(new Poem("THE world puts off its mask of vastness to its lover."));
        poems.add(new Poem("It becomes small as one song, as one kiss of the eternal."));
        poems.add(new Poem("IT is the tears of the earth that keep her smiles in bloom."));
        poems.add(new Poem("THE mighty desert is burning for the love of a blade of grass who shakes her head and laughs and flies away."));
        poems.add(new Poem("IF you shed tears when you miss the sun, you also miss the stars."));
        poems.add(new Poem("THE sands in your way beg for your song and your movement, dancing water. Will you carry the burden of their lameness?"));
        poems.add(new Poem("HER wistful face haunts my dreams like the rain at night."));
        poems.add(new Poem("SORROW is hushed into peace in my heart like the evening among the silent trees."));
        poems.add(new Poem("SOME unseen fingers, like idle breeze, are playing upon my heart the music of the ripples."));
        poems.add(new Poem("LISTEN, my heart, to the whispers of the world with which it makes love to you."));
        poems.add(new Poem("THE mystery of creation is like the darkness of night--it is great. Delusions of knowledge are like the fog of the morning."));
        poems.add(new Poem("DO not seat your love upon a precipice because it is high."));
        poems.add(new Poem("I SIT at my window this morning where the world like a passer-by stops for a moment, nods to me and goes."));
        poems.add(new Poem("THESE little thoughts are the rustle of leaves; they have their whisper of joy in my mind."));
        poems.add(new Poem("WHAT you are you do not see, what you see is your shadow."));
        poems.add(new Poem("MY wishes are fools, they shout across thy songs, my Master.Let me but listen."));
        poems.add(new Poem("I CANNOT choose the best. The best chooses me."));
        poems.add(new Poem("THEY throw their shadows before them who carry their lantern on their back."));
        poems.add(new Poem("THAT I exist is a perpetual surprise which is life."));
        poems.add(new Poem("MAN is a born child, his power is the power of growth."));
        poems.add(new Poem("GOD expects answers for the flowers he sends us, not for the sun and the earth."));
        poems.add(new Poem("THE light that plays, like a naked child, among the green leaves happily knows not that man can lie."));
        poems.add(new Poem("O BEAUTY, find thyself in love, not in the flattery of thy mirror."));
        poems.add(new Poem("MY heart beats her waves at the shore of the world and writes upon it her signature in tears with the words, I love thee."));
        poems.add(new Poem("THE trees come up to my window like the yearning voice of the dumb earth."));
        poems.add(new Poem("LIFE finds its wealth by the claims of the world, and its worth by the claims of love."));
        poems.add(new Poem("THE bird wishes it were a cloud. The cloud wishes it were a bird."));
        poems.add(new Poem("THE trees, like the longings of the earth, stand a-tiptoe to peep at the heaven."));
        poems.add(new Poem("YOU smiled and talked to me of nothing and I felt that for this I had been waiting long."));
        poems.add(new Poem("THE world rushes on over the strings of the lingering heart making the music of sadness."));
        poems.add(new Poem("SHADOW, with her veil drawn, follows Light in secret meekness, with her silent steps of love."));
        poems.add(new Poem("THE stars are not afraid to appear like fireflies."));
        poems.add(new Poem("I THANK thee that I am none of the wheels of power but I am one with the living creatures that are crushed by it."));
        poems.add(new Poem("THE mind, sharp but not broad, sticks at every point but does not move."));
        poems.add(new Poem("MY day is done, and I am like a boat drawn on the beach, listening to the dance-music of the tide in the evening."));
        poems.add(new Poem("THE hurricane seeks the shortest road by the no-road, and suddenly ends its search in the Nowhere."));
        poems.add(new Poem("WHERE is the fountain that throws up these flowers in a ceaseless outbreak of ecstasy?"));
        poems.add(new Poem("THE mist, like love, plays upon the heart of the hills and brings out surprises of beauty."));
        poems.add(new Poem("IN my solitude of heart I feel the sigh of this widowed evening veiled with mist and rain."));
        poems.add(new Poem("THE poet wind is out over the sea and the forest to seek his own voice."));
        poems.add(new Poem("EVERY child comes with the message that God is not yet discouraged of man."));
        poems.add(new Poem("THE grass seeks her crowd in the earth. The tree seeks his solitude of the sky."));
        poems.add(new Poem("MAN barricades against himself."));
        poems.add(new Poem("YOUR voice, my friend, wanders in my heart, like the muffled sound of the sea among these listening pines."));
        poems.add(new Poem("WHAT is this unseen flame of darkness whose sparks are the stars?"));
        poems.add(new Poem("LET life be beautiful like summer flowers and death like autumn leaves."));
        poems.add(new Poem("HE who wants to do good knocks at the gate; he who loves finds the gate open."));
        poems.add(new Poem("IN death the many becomes one; in life the one becomes many."));
        poems.add(new Poem("THE artist is the lover of Nature, therefore he is her slave and her master."));
        poems.add(new Poem("THIS longing is for the one who is felt in the dark, but not seen in the day."));
        poems.add(new Poem("IN darkness the One appears as uniform; in the light the One appears as manifold."));
        poems.add(new Poem("THE birth and death of the leaves are the rapid whirls of the eddy whose wider circles move slowly among stars."));
        poems.add(new Poem("THE mist is like the earth's desire. It hides the sun for whom she cries."));
        poems.add(new Poem("THE noise of the moment scoffs at the music of the Eternal."));
        poems.add(new Poem("I THINK of other ages that floated upon the stream of life and love and death and are forgotten, and I feel the freedom of passing away."));
        poems.add(new Poem("DEATH'S stamp gives value to the coin of life; making it possible to buy with life what is truly precious."));
        // Add more poems as necessary
    }
    public String RandomWin() {

        return "哈哈，居然让我赢了！";
    }


    public Poem getRandomPoem() {
        int index = random.nextInt(poems.size());
        return poems.get(index);
    }
}
