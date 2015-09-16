#!/usr/bin/python
import os
import shutil
import subprocess
import sys
import unittest
import xml.etree.ElementTree

import xmlrunner

def run_gtest(name):
    xml_fname = name.replace(os.path.sep, "_")
    subprocess.check_call("%s --gtest_output=xml:lcm_unittest/%s.xml" % \
            (name, xml_fname), shell=True)

def run_tests():
    # Python unit tests
    print("Running Python unit tests")
    py_results_file = open(os.path.join("lcm_unittest", "lcm_python.xml"), "w")
    sys.path.append("python")
    tests = unittest.TestLoader().discover("python", "*_test.py")
    runner = xmlrunner.XMLTestRunner(py_results_file)
    runner.run(tests, "LCM_PYTHON")
    py_results_file.close()

    # C unit tests
    print("Running C unit tests")
    run_gtest(os.path.join("c", "memq_test"))
    run_gtest(os.path.join("c", "eventlog_test"))

    # C++ unit tests
    print("Running C++ unit tests")
    run_gtest(os.path.join("cpp", "memq_test"))

def summarize_results():
    # Parse and summarize unit test results
    num_failures = 0
    num_tests = 0
    num_errors = 0
    for bname in os.listdir("lcm_unittest"):
        fname = os.path.join("lcm_unittest", bname)
        tree = xml.etree.ElementTree.parse(fname)
        root = tree.getroot()
        for testsuite in root.iter("testsuite"):
            print("Test suite: %s" % testsuite.attrib["name"])
            suite_attrib = testsuite.attrib
            num_failures += int(suite_attrib["failures"])
            num_tests += int(suite_attrib["tests"])
            num_errors += int(suite_attrib["tests"])
            for testcase in testsuite.iter("testcase"):
                failures = list(testcase.iter("failure"))

                if not failures:
                    print("      OK   %s.%s" % (testsuite.attrib["name"],
                            testcase.attrib["name"]))
                else:
                    print(" FAIL      %s.%s" % (testsuite.attrib["name"],
                            testcase.attrib["name"]))
                    for failure in failures:
                        print(failure.text)
        print("")

    print("Tests passed: %d / %d" % (num_tests - num_failures, num_tests))

    if num_failures:
        print("Not all tests passed")
        return 1
    else:
        print("All unit tests passed")
        return 0

def main():
    # Setup output directory
    if os.path.exists("lcm_unittest"):
        shutil.rmtree("lcm_unittest")

    os.mkdir("lcm_unittest")

    run_tests()
    return summarize_results()

if __name__ == "__main__":
    sys.exit(main())
