# coding=utf-8
import sys, imp, marshal, json, new, time, types
from opcode import *


def convertjsontocode(obj):
    if type(obj) is str:
        jsonObj = json.loads(obj)
    else:
        jsonObj = obj

    myConst = []
    for cons in jsonObj["myConst"]:
        if type(cons) is dict:
            myConst.append(convertjsontocode(cons))
        else:
            myConst.append(switchConst(cons))
    myConst = tuple(myConst)

    names = []
    for name in jsonObj["names"]:
        names.append(str(name))
    names = tuple(names)

    varNames = []
    for varname in jsonObj["varNames"]:
        varNames.append(str(varname))
    varNames = tuple(varNames)

    code = new.code(jsonObj["argCount"],
                    jsonObj["nLocals"],
                    jsonObj["stackSize"],
                    int(jsonObj["flags"]),
                    jsonObj["code"].decode('hex'),
                    myConst,
                    names,
                    varNames,
                    str(jsonObj["fileName"]),
                    str(jsonObj["name"]),
                    jsonObj["firstLineNumber"],
                    str(jsonObj["lNoTab"])
                    )

    return code


def switchConst(const):
    strConst = str(const)
    if strConst == 'true':
        return True

    if strConst == 'false':
        return False

    if strConst == 'None':
        return None

    try:
        return int(strConst)
    except ValueError:
        return strConst


def isnumber(s):
    try:
        int(s)
        return True
    except ValueError:
        return False


def savepyc(code, path):
    stamp = long(time.time())
    magicnum = imp.get_magic()  # python 버전별 매직 넘버
    dumpCode = marshal.dumps(code)
    try:
        with open(path, 'wb') as f:
            f.write(magicnum)
            f.write(chr(stamp & 0xff))
            f.write(chr((stamp >> 8) & 0xff))
            f.write(chr((stamp >> 16) & 0xff))
            f.write(chr((stamp >> 24) & 0xff))
            f.write(dumpCode)
            f.flush()
    except OSError:
        try:
            print "Error!"
        except OSError:
            pass
        raise


def disassemCompiledFile(fileName):
    f = open(fileName, "rb")
    magic = f.read(4)
    moddate = f.read(4)
    print "magic %s" % (magic.encode('hex'))
    print "moddate %s" % (moddate.encode('hex'))
    code = marshal.load(f)
    showCode(code)


def showCode(code, indent=''):
    print "%scode" % indent
    indent += '   '
    print "%sargcount \t:%d" % (indent, code.co_argcount)
    print "%snlocals   \t:%d" % (indent, code.co_nlocals)
    print "%sstacksize \t:%d" % (indent, code.co_stacksize)
    print "%sflags     \t:%04x" % (indent, code.co_flags)
    showHex("code", code.co_code, indent=indent)
    disassemble(code)
    print "%sconsts" % indent
    for const in code.co_consts:
        if type(const) == types.CodeType:
            showCode(const, indent + '   ')
        else:
            print "   %s%r" % (indent, const)
    print "%snames %r" % (indent, code.co_names)
    print "%svarnames %r" % (indent, code.co_varnames)
    print "%sfreevars %r" % (indent, code.co_freevars)
    print "%scellvars %r" % (indent, code.co_cellvars)
    print "%sfilename %r" % (indent, code.co_filename)
    print "%sname %r" % (indent, code.co_name)
    print "%sfirstlineno %d" % (indent, code.co_firstlineno)


def disassemble(co):
    code = co.co_code
    n = len(code)
    i = 0
    free = None
    while i < n:
        c = code[i]
        op = ord(c)
        print '        ',
        print repr(i).rjust(4),
        print opname[op].ljust(20),
        i = i + 1
        if op >= HAVE_ARGUMENT:
            oparg = ord(code[i]) + ord(code[i + 1]) * 256
            i = i + 2
            print repr(oparg).rjust(5),
            if op in hasconst:
                print '(' + repr(co.co_consts[oparg]) + ')',
            elif op in hasname:
                print '(' + co.co_names[oparg] + ')',
            elif op in haslocal:
                print '(' + co.co_varnames[oparg] + ')',
            elif op in hascompare:
                print '(' + cmp_op[oparg] + ')',
            elif op in hasfree:
                if free is None:
                    free = co.co_cellvars + co.co_freevars
                print '(' + free[oparg] + ')',
        print


def showHex(label, h, indent):
    h = h.encode('hex')
    if len(h) < 60:
        print "%s%s %s" % (indent, label, h)
    else:
        print "%s%s" % (indent, label)
        for i in range(0, len(h), 60):
            print "%s   %s" % (indent, h[i:i + 60])


def main(argv, path):
    code = convertjsontocode(argv)
    savepyc(code, path)
    disassemCompiledFile(path)


if __name__ == "__main__":
    print "JSON : \n" + sys.argv[1]
    main(sys.argv[1], "./test.pyc")
