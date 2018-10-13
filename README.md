# Clojure Playground

One off namespaces playing around with some clojure concepts. Mostly from the 2nd Edition of [The Joy of Clojure](http://www.joyofclojure.com/).

## Usage

Since these are one-off experiments, I mostly throw things in one namespace and interact with it from a CIDER repl in my Emacs, for example, for the Sudoku solver from Chapter 16 of the book:

![image](https://user-images.githubusercontent.com/82133/46909158-e7733380-cefb-11e8-9eb7-b694fe697ab5.png)



### An Emacs aside

My current set of packages and colors is pretty much straight out of the [Clojure for the Brave and True](https://www.braveclojure.com/basic-emacs/) Emacs tutorial, as [packaged by the author](https://github.com/flyingmachine/emacs-for-clojure/). Caveat Emptor (?)

Using [CIDER](https://cider.readthedocs.io/en/latest/using_the_repl/) means that I can edit my functions in a file in one buffer, and run them in another. Some things to note:

* Upon start, CIDER will load the dependencies in the namespace's `project.clj`. If you add new dependencies, you'll want to `M-x cider-quit` and then call `M-x cider-jack-in` from a file in the projet. I believe `M-x cider-restart` should work as well
* My workflow is usually: write or edit a function, run `C-c C-k` to compile the buffer (which saves the file, too) and then, if I'm not already there, run `C-c M-n` to switch the REPL's context to my current namespace. `M-p` is useful when going back to previous sexps, in case I've changed a function, and Paredit's slurp and barf help too (`C-)` and `C-}`, respectively).
