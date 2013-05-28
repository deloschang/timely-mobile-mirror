package dartmouth.timely;

class WekaClassifier {
    
    public static double classify(Object[] i)
    throws Exception {
        
        double p = Double.NaN;
        p = WekaClassifier.N483bead50(i);
        return p;
    }
    static double N483bead50(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 59.418692) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 59.418692) {
            p = WekaClassifier.N55b66aff1(i);
        } 
        return p;
    }
    static double N55b66aff1(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 452.296058) {
            p = WekaClassifier.N3b2ab74f2(i);
        } else if (((Double) i[0]).doubleValue() > 452.296058) {
            p = WekaClassifier.N1490d4f24(i);
        } 
        return p;
    }
    static double N3b2ab74f2(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() <= 61.532001) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() > 61.532001) {
            p = WekaClassifier.N7168c1e13(i);
        } 
        return p;
    }
    static double N7168c1e13(Object []i) {
        double p = Double.NaN;
        if (i[2] == null) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() <= 72.858782) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() > 72.858782) {
            p = 2;
        } 
        return p;
    }
    static double N1490d4f24(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 2;
        } else if (((Double) i[64]).doubleValue() <= 19.779505) {
            p = WekaClassifier.N308c666a5(i);
        } else if (((Double) i[64]).doubleValue() > 19.779505) {
            p = 2;
        } 
        return p;
    }
    static double N308c666a5(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 2;
        } else if (((Double) i[4]).doubleValue() <= 37.726902) {
            p = WekaClassifier.N5197d20c6(i);
        } else if (((Double) i[4]).doubleValue() > 37.726902) {
            p = WekaClassifier.N1646fef38(i);
        } 
        return p;
    }
    static double N5197d20c6(Object []i) {
        double p = Double.NaN;
        if (i[6] == null) {
            p = 1;
        } else if (((Double) i[6]).doubleValue() <= 8.207564) {
            p = WekaClassifier.N465f3bad7(i);
        } else if (((Double) i[6]).doubleValue() > 8.207564) {
            p = 2;
        } 
        return p;
    }
    static double N465f3bad7(Object []i) {
        double p = Double.NaN;
        if (i[2] == null) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() <= 113.152034) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() > 113.152034) {
            p = 2;
        } 
        return p;
    }
    static double N1646fef38(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if (((Double) i[4]).doubleValue() <= 63.116754) {
            p = WekaClassifier.Nf0c0ef29(i);
        } else if (((Double) i[4]).doubleValue() > 63.116754) {
            p = 2;
        } 
        return p;
    }
    static double Nf0c0ef29(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 539.511858) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 539.511858) {
            p = WekaClassifier.N60a9399510(i);
        } 
        return p;
    }
    static double N60a9399510(Object []i) {
        double p = Double.NaN;
        if (i[8] == null) {
            p = 1;
        } else if (((Double) i[8]).doubleValue() <= 10.842532) {
            p = 1;
        } else if (((Double) i[8]).doubleValue() > 10.842532) {
            p = 2;
        } 
        return p;
    }
}
